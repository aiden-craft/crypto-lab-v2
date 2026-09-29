/* Crypto Lab V2 — 화면 상태만 브라우저에 저장합니다. 평문, 비밀번호, 키는 저장하지 않습니다. */
(() => {
  'use strict';
  const ids = ['hash','avalanche','bulk','rainbow','aes','rsa','hmac','pbkdf2','benchmark'];
  const names = ['Hash와 Salt','Avalanche Effect','Bulk Hash','사전 공격 실험','AES 대칭키','RSA 공개키','HMAC 무결성','PBKDF2 저장','Benchmark'];
  const storageKey = 'crypto-lab:v2:progress';
  const state = {
    done: new Set(), last: 'hash', busy: null, actions: {},
    hash: null, dictionary: false, aes: null, rsa: null, hmac: null, pb: null
  };
  const $ = id => document.getElementById(id);
  const value = id => $(id).value;
  const node = (tag, className, content) => {
    const el = document.createElement(tag);
    if (className) el.className = className;
    if (content !== undefined && content !== null) el.textContent = String(content);
    return el;
  };
  function put(parent, ...children) { children.forEach(child => parent.append(child)); return parent; }
  function number(valueToFormat) { return Number(valueToFormat).toLocaleString('ko-KR'); }
  function bytes(text) { return new TextEncoder().encode(text).length; }
  function toast(message) {
    const el = $('toast');
    el.textContent = message;
    el.classList.add('show');
    clearTimeout(toast.timer);
    toast.timer = setTimeout(() => el.classList.remove('show'), 2600);
  }
  function loadProgress() {
    try {
      const saved = JSON.parse(localStorage.getItem(storageKey) || '{}');
      state.done = new Set((Array.isArray(saved.done) ? saved.done : []).filter(id => ids.includes(id)));
      state.last = ids.includes(saved.last) ? saved.last : 'hash';
    } catch (_) { state.done = new Set(); }
  }
  function saveProgress() {
    try { localStorage.setItem(storageKey, JSON.stringify({ done: [...state.done], last: state.last })); } catch (_) {}
  }
  function updateProgress() {
    const count = state.done.size;
    $('progressText').textContent = count + ' / ' + ids.length;
    $('progressFill').style.width = (count / ids.length * 100) + '%';
    $('progressBar').setAttribute('aria-valuenow', String(count));
    $('progressHint').textContent = count === ids.length ? '모든 실습을 완료했습니다!' : count ? '다음 관찰도 이어가 보세요.' : '첫 실습을 시작해 보세요.';
    document.querySelectorAll('.course-nav button[data-go]').forEach(button => {
      button.classList.toggle('done', state.done.has(button.dataset.go));
    });
    const next = ids.find(id => !state.done.has(id)) || state.last;
    $('resumeButton').dataset.go = state.done.size ? next : 'hash';
    $('resumeButton').firstChild.textContent = state.done.size ? '이어서 실습하기 ' : '첫 실습 시작 ';
  }
  function complete(id) {
    if (!state.done.has(id)) { state.done.add(id); saveProgress(); updateProgress(); toast(names[ids.indexOf(id)] + ' 실습 완료'); }
  }
  function closeMenu() {
    $('sidebar').classList.remove('open');
    $('sidebarScrim').hidden = true;
    $('menuButton').setAttribute('aria-expanded', 'false');
  }
  function navigate(id, updateUrl = true) {
    if (id !== 'home' && !ids.includes(id)) id = 'home';
    document.querySelectorAll('.page').forEach(page => page.classList.toggle('active', page.id === 'page-' + id));
    document.querySelectorAll('.course-nav button[data-go]').forEach(button => button.classList.toggle('active', button.dataset.go === id));
    $('crumbCurrent').textContent = id === 'home' ? '대시보드' : names[ids.indexOf(id)];
    if (id !== 'home') { state.last = id; saveProgress(); }
    if (updateUrl && location.hash !== '#' + id) history.pushState(null, '', '#' + id);
    closeMenu();
    window.scrollTo({ top: 0, behavior: 'auto' });
    if (updateUrl) {
      const heading = $('page-' + id).querySelector('h1');
      heading.tabIndex = -1;
      heading.focus({ preventScroll: true });
    }
  }
  function setEnabled(lab, action, enabled) {
    state.actions[action] = enabled;
    const button = document.querySelector('#page-' + lab + ' [data-action="' + action + '"]');
    if (button) button.disabled = !enabled || state.busy === lab;
  }
  function clearInvalid(id) { $(id).removeAttribute('aria-invalid'); }
  function textRequired(id, label, max = 10000) {
    const text = value(id);
    if (!text.trim() || text.length > max) {
      $(id).setAttribute('aria-invalid', 'true'); $(id).focus();
      throw new Error(label + '을(를) 1~' + number(max) + '자로 입력하세요.');
    }
    clearInvalid(id);
    return text;
  }
  function integerRange(id, label, min, max) {
    const raw = value(id);
    const n = Number(raw);
    if (!raw || !Number.isInteger(n) || n < min || n > max) {
      $(id).setAttribute('aria-invalid', 'true'); $(id).focus();
      throw new Error(label + '은(는) ' + number(min) + '~' + number(max) + ' 사이의 정수로 입력하세요.');
    }
    clearInvalid(id);
    return n;
  }
  async function api(url, options = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 90000);
    try {
      const response = await fetch(url, { ...options, signal: controller.signal });
      let body;
      try { body = await response.json(); } catch (_) { body = null; }
      if (!response.ok) {
        const error = new Error(response.status === 400 ? '입력값을 확인하세요.' : '요청을 처리하지 못했습니다. 입력값과 서버 로그를 확인하세요.');
        error.status = response.status;
        throw error;
      }
      if (!body) throw new Error('서버에서 올바른 JSON 결과를 받지 못했습니다.');
      return body;
    } catch (error) {
      if (error.name === 'AbortError') throw new Error('처리 시간이 길어 요청을 중단했습니다. 실행 규모를 줄여 다시 시도하세요.');
      if (error instanceof TypeError) throw new Error('서버에 연결할 수 없습니다. Spring Boot 실행 상태를 확인하세요.');
      throw error;
    } finally { clearTimeout(timer); }
  }
  function post(url, body) { return api(url, { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) }); }
  function query(path, parameters) { return path + '?' + new URLSearchParams(parameters); }
  function resultShell(id, status = '대기 중', tone = '') {
    const container = $('result-' + id);
    container.replaceChildren();
    const head = node('div', 'result-top');
    put(head, node('h2', '', '실습 결과'), node('span', 'state-pill ' + tone, status));
    container.append(head);
    return container;
  }
  function empty(id) {
    const root = resultShell(id);
    const box = node('div', 'empty-state');
    put(box, node('span', 'empty-icon', '◇'), node('strong', '', '결과가 여기에 나타납니다'), node('p', '', '왼쪽에서 값을 입력하고 실행해 보세요. 결과의 차이를 이 화면에서 설명합니다.'));
    root.append(box);
  }
  function loading(id) {
    const root = resultShell(id, '처리 중', 'loading');
    const box = node('div', 'empty-state');
    put(box, node('span', 'spinner'), node('strong', '', '계산 중입니다'), node('p', '', '입력값에 따른 결과를 준비하고 있습니다.'));
    root.append(box);
  }
  function outcome(root, tone, heading, explanation) {
    const box = node('div', 'outcome ' + tone);
    const copy = node('div');
    put(copy, node('strong', '', heading), node('small', '', explanation));
    put(box, node('span', 'symbol', tone === 'success' ? '✓' : tone === 'error' ? '!' : 'i'), copy);
    root.append(box);
  }
  function metric(root, items) {
    const grid = node('div', 'metrics');
    items.forEach(([label, val]) => { const card = node('div', 'metric'); put(card, node('span', '', label), node('strong', '', val)); grid.append(card); });
    root.append(grid);
  }
  function label(root, caption) { root.append(node('div', 'result-label', caption)); }
  function compare(root, left, right) {
    const wrap = node('div', 'comparison');
    [left, right].forEach(([name, val]) => {
      const box = node('div', 'value-card');
      put(box, node('div', 'value-head', name), node('code', '', val));
      wrap.append(box);
    });
    root.append(wrap);
  }
  function code(root, caption, text, allowCopy = true) {
    const box = node('div', 'code-block');
    const title = node('label', '', caption);
    if (allowCopy) {
      const button = node('button', 'copy-value', '복사');
      button.type = 'button'; button.dataset.copy = text;
      title.append(button);
    }
    put(box, title, node('div', 'code-value', text));
    root.append(box);
  }
  function lesson(root, message) { const box = node('div', 'lesson'); put(box, node('strong', '', '기억할 점'), node('p', '', message)); root.append(box); }
  function raw(root, data) {
    if (!data) return;
    const detail = node('details', 'raw-response');
    put(detail, node('summary', '', '실제 API 응답 보기'), node('pre', '', JSON.stringify(data, null, 2)));
    root.append(detail);
  }
  function render(id, status, tone, heading, explanation, content, response) {
    const root = resultShell(id, status, tone);
    outcome(root, tone, heading, explanation);
    if (content) content(root);
    raw(root, response);
  }
  async function run(id, fn) {
    if (state.busy) return;
    state.busy = id;
    document.querySelectorAll('#page-' + id + ' [data-action]').forEach(button => button.disabled = true);
    loading(id);
    try { await fn(); }
    catch (error) {
      render(id, '확인 필요', 'error', '실행하지 못했습니다', error.message || '다시 시도하세요.');
    } finally {
      state.busy = null;
      document.querySelectorAll('#page-' + id + ' [data-action]').forEach(button => button.disabled = !state.actions[button.dataset.action]);
    }
  }
  function makeNavigation() {
    ids.forEach((id, index) => {
      const nav = node('div', 'button-row lab-bottom');
      if (index > 0) { const prev = node('button', 'button subtle', '← 이전 실습'); prev.type = 'button'; prev.dataset.go = ids[index - 1]; nav.append(prev); }
      const next = node('button', 'button secondary', index === ids.length - 1 ? '대시보드로 ↗' : '다음 실습 →');
      next.type = 'button'; next.dataset.go = ids[index + 1] || 'home'; nav.append(next);
      $('page-' + id).append(nav);
    });
  }
  async function hash() {
    const plain = textRequired('hashPlain', '평문', 2000);
    const salt = textRequired('hashSalt', 'Salt', 200);
    const algorithm = value('hashAlgo');
    const [base, salted] = await Promise.all([
      post('/api/hash/generate', { plainText: plain, salt: '', algorithm }),
      post('/api/hash/generate', { plainText: plain, salt, algorithm })
    ]);
    state.hash = base.hashValue;
    render('hash', '비교 완료', 'success', 'Salt가 출력값을 바꿨습니다',
      '같은 평문과 알고리즘을 사용했고 Salt만 달리했습니다.', root => {
        metric(root, [['출력 길이', base.hashValue.length * 4 + '비트'], ['두 결과', base.hashValue === salted.hashValue ? '동일' : '서로 다름']]);
        label(root, 'SALT 적용 전 / 후');
        compare(root, ['Salt 없음', base.hashValue], ['Salt 적용', salted.hashValue]);
        lesson(root, 'Salt는 해시의 출력 길이를 바꾸지 않습니다. 비밀번호 저장에는 임의 Salt와 PBKDF2 같은 전용 함수를 사용합니다.');
      }, { withoutSalt: base, withSalt: salted });
    complete('hash');
  }
  async function avalanche() {
    const original = textRequired('avOriginal', '원본', 2000);
    const modified = textRequired('avModified', '변경 후', 2000);
    const data = await api(query('/api/hash/avalanche', { original, modified, algorithm: value('avAlgo') }));
    const bits = data.differentBits;
    const total = data.totalBits;
    const ratio = total ? bits / total * 100 : 0;
    const changed = original !== modified;
    render('avalanche', changed ? '비교 완료' : '입력 동일', changed ? 'success' : 'warning',
      changed ? '작은 변화가 해시 전체로 퍼졌습니다' : '두 입력이 같습니다',
      changed ? '서로 달라진 비트의 비율을 확인하세요.' : '한 글자를 변경해 다시 실행하면 눈사태 효과를 볼 수 있습니다.', root => {
        metric(root, [['달라진 비트', number(bits) + ' / ' + number(total)], ['변화율', ratio.toFixed(1) + '%']]);
        label(root, '두 SHA 해시');
        compare(root, ['원본', data.originalHash], ['변경 후', data.modifiedHash]);
        lesson(root, changed ? '이는 두 출력의 비트 차이입니다. 단일 비교의 비율이 항상 정확히 50%가 되는 것은 아닙니다.' : '같은 입력은 같은 해시를 만듭니다.');
      }, data);
    if (changed) complete('avalanche');
  }
  async function bulk() {
    const count = integerRange('bulkCount', '생성 개수', 1, 50000);
    const length = integerRange('bulkLength', '문자 길이', 1, 64);
    const data = await api(query('/api/hash/bulk', { count, length, algorithm: value('bulkAlgo'), useSalt: $('bulkSalt').checked }));
    const duplicateResults = data.totalCount - data.uniqueHashCount;
    render('bulk', '측정 완료', 'success', number(data.totalCount) + '개 생성 완료',
      '생성한 입력에 대한 해시 결과를 모아 고유 개수를 셌습니다.', root => {
        metric(root, [['고유 해시 수', number(data.uniqueHashCount)], ['중복 관찰 수', number(duplicateResults)], ['처리 시간', number(data.elapsedMillis) + ' ms'], ['입력 길이', length + '자']]);
        lesson(root, '중복 결과가 있다 해도 입력 자체가 중복되었는지 확인해야 충돌 여부를 판단할 수 있습니다. 중복이 없다는 사실도 충돌이 불가능하다는 증명은 아닙니다.');
      }, data);
    complete('bulk');
  }
  async function dictionary() {
    const data = await api('/api/rainbow/generate/common', { method: 'POST' });
    state.dictionary = true;
    render('rainbow', '사전 준비', 'success', number(data.generatedSize) + '개 항목 준비',
      '이제 SHA-256 해시를 입력해 사전에서 원문을 찾아보세요.', root => {
        metric(root, [['저장된 해시', number(data.generatedSize) + '개']]);
        lesson(root, '사전에는 Salt를 적용하지 않은 SHA-256 결과가 들어 있습니다. 비밀번호를 복호화하는 과정은 아닙니다.');
      }, data);
  }
  async function dictionaryClear() {
    const data = await api('/api/rainbow', { method: 'DELETE' });
    state.dictionary = false;
    render('rainbow', '초기화 완료', 'warning', '사전을 비웠습니다', '다시 생성한 뒤 조회해 보세요.', root => {
      lesson(root, '메모리에 만든 사전이 비워졌습니다. 완료한 학습 진행 기록은 유지됩니다.');
    }, data);
  }
  async function lookupDictionary(input, experiment) {
    if (!state.dictionary) throw new Error('먼저 공통 비밀번호 사전을 생성하세요.');
    if (!/^[0-9a-f]{64}$/.test(input)) {
      $('rainbowHash').setAttribute('aria-invalid', 'true');
      throw new Error('64자리 SHA-256 16진수 해시를 입력하세요.');
    }
    const data = await api(query('/api/rainbow/lookup', { hash: input }));
    render('rainbow', data.found ? '일치 항목 발견' : '일치 항목 없음', data.found ? 'success' : 'warning',
      data.found ? '사전에서 원문을 찾았습니다' : '사전에서 찾지 못했습니다',
      data.found ? '같은 해시를 미리 계산해 둔 항목이 있었습니다.' : '사전에 없는 값이거나 Salt를 적용한 해시일 수 있습니다.', root => {
        metric(root, [['검색 결과', data.found ? '발견' : '미발견'], ['Salt', experiment ? (experiment.salt ? '적용' : '미적용') : '직접 해시 입력']]);
        if (experiment) code(root, '직접 입력한 평문', experiment.plainText, false);
        code(root, '조회한 SHA-256 해시', input);
        if (data.found) code(root, '대조 결과 원문', data.plainText);
        lesson(root, '이 과정은 해시 역연산이 아닙니다. 동일한 해시를 가진 사전 항목과 대조합니다. Salt를 적용하면 Salt 없는 사전과는 일치하지 않습니다.');
      }, { experiment: experiment || null, lookup: data });
    complete('rainbow');
  }
  async function dictionaryPlain() {
    const plainText = textRequired('rainbowPlain', '시험할 평문', 500);
    const salt = value('rainbowSalt');
    if (salt.length > 200) throw new Error('Salt는 200자 이내로 입력하세요.');
    if (!state.dictionary) throw new Error('먼저 공통 비밀번호 사전을 생성하세요.');
    const generated = await post('/api/hash/generate', { plainText, salt, algorithm: 'SHA_256' });
    $('rainbowHash').value = generated.hashValue;
    await lookupDictionary(generated.hashValue, { plainText, salt });
  }
  async function dictionaryLookup() {
    const input = textRequired('rainbowHash', '조회할 해시', 128).trim().toLowerCase();
    await lookupDictionary(input, null);
  }
  async function aesEncrypt(isAgain = false) {
    let input;
    if (isAgain) {
      if (!state.aes) throw new Error('먼저 암호화를 실행하세요.');
      input = state.aes.input;
    } else {
      input = { plainText: textRequired('aesPlain', '평문', 5000), secretKey: textRequired('aesKey', '비밀키', 500), ivMode: value('aesMode'), ivSeed: value('aesSeed') };
    }
    const data = await post('/api/aes/encrypt', input);
    const previous = isAgain ? state.aes.data : null;
    state.aes = { input, data, firstCipher: isAgain ? state.aes.firstCipher : data.cipherText };
    $('aesDecCipher').value = data.cipherText;
    $('aesDecIv').value = data.ivBase64;
    $('aesDecKey').value = input.secretKey;
    $('aesDecryptInputs').hidden = false;
    ['aes-decrypt','aes-again'].forEach(action => setEnabled('aes', action, true));
    render('aes', isAgain ? '재암호화 완료' : '암호화 완료', 'success',
      isAgain ? (previous.cipherText === data.cipherText ? '암호문이 같습니다' : '새 암호문이 생성됐습니다') : '평문이 암호문으로 바뀌었습니다',
      isAgain ? '동일한 평문과 키를 다시 사용했습니다.' : '복호화 또는 같은 값으로 재암호화를 실행해 보세요.', root => {
        if (previous) { label(root, '이전 / 이번 암호문'); compare(root, ['이전', previous.cipherText], ['이번', data.cipherText]); }
        else code(root, '암호문 · Base64', data.cipherText);
        metric(root, [['IV 방식', data.mode === 'DERIVED' ? '고정 파생' : '임의 생성'], ['암호문 비교', previous ? (previous.cipherText === data.cipherText ? '동일' : '다름') : '첫 생성']]);
        code(root, 'IV · Base64', data.ivBase64);
        lesson(root, data.mode === 'DERIVED' ? '고정 IV는 패턴을 노출할 수 있어 이 화면의 비교 학습에만 사용합니다.' : '임의 IV를 재생성하면 같은 평문과 키에도 다른 암호문이 나옵니다. IV는 비밀키와 다릅니다.');
      }, { cipherText: data.cipherText, ivBase64: data.ivBase64, mode: data.mode, note: data.note });
  }
  async function aesDecrypt() {
    if (!state.aes) throw new Error('먼저 암호화하세요.');
    const saved = state.aes;
    const params = {
      cipherText: textRequired('aesDecCipher', '암호문', 10000).trim(),
      ivBase64: textRequired('aesDecIv', 'IV', 500).trim(),
      secretKey: textRequired('aesDecKey', '복호화 비밀키', 500)
    };
    const altered = params.cipherText !== saved.data.cipherText ||
      params.ivBase64 !== saved.data.ivBase64 || params.secretKey !== saved.input.secretKey;
    try {
      const data = await post('/api/aes/decrypt', params);
      const match = data.plainText === saved.input.plainText;
      render('aes', match ? '복호화 성공' : '평문 불일치', match ? 'success' : 'warning',
        match ? '원본 평문을 복원했습니다' : '복호화 값이 원본과 다릅니다',
        altered ? '편집한 복호화 값을 사용했습니다. AES-CBC는 인증 기능이 없으므로 결과를 원문과 비교해야 합니다.' : '같은 키와 IV로 암호문을 복호화했습니다.', root => {
          compare(root, ['입력 평문', saved.input.plainText], ['복호화 평문', data.plainText]);
          lesson(root, '암호화와 무결성 검증은 다른 목적입니다. AES-CBC의 복호화 성공만으로 위변조가 없다고 판단할 수 없습니다.');
        }, data);
      if (match) complete('aes');
    } catch (error) {
      if (!altered || !error.status) throw error;
      render('aes', '의도된 실패', 'warning', '편집한 값으로 복호화에 실패했습니다',
        '암호문·IV·키 중 변경한 값으로 복호화를 시도했습니다.', root => {
          lesson(root, '패딩 오류로 실패하는 것이 일반적이지만, AES-CBC 자체가 변조를 인증하는 방식은 아닙니다.');
        }, { expectedFailure: true, httpStatus: error.status });
    }
  }
  async function rsaKeys() {
    const data = await api('/api/rsa/keys', { method: 'POST' });
    state.rsa = { publicKey: data.publicKey, privateKey: data.privateKey, first: '', second: '', plainText: '' };
    setEnabled('rsa', 'rsa-compare', true);
    setEnabled('rsa', 'rsa-limit', true);
    ['rsa-decrypt','rsa-other-key'].forEach(action => setEnabled('rsa', action, false));
    $('rsaDecryptInputs').hidden = true;
    render('rsa', '키 생성 완료', 'success', '공개키와 개인키를 만들었습니다',
      '이제 공개키로 같은 평문을 두 번 암호화해 보세요.', root => {
        metric(root, [['키 길이', '2,048비트'], ['다음 단계', '암호화 비교']]);
        code(root, '공개키 · 앞부분', data.publicKey.slice(0, 56) + '…', false);
        lesson(root, '개인키는 화면에 표시하지 않습니다. 이 실습에서는 브라우저 메모리에만 보관합니다.');
      }, { publicKey: data.publicKey, privateKey: '(숨김)' });
  }
  async function rsaCompare() {
    if (!state.rsa) throw new Error('먼저 키 쌍을 생성하세요.');
    const plainText = textRequired('rsaPlain', '평문', 500);
    if (bytes(plainText) > 190) { $('rsaPlain').setAttribute('aria-invalid', 'true'); throw new Error('평문은 UTF-8 기준 190바이트 이내로 입력하세요. 제한 실험은 아래 버튼을 사용하세요.'); }
    const params = { plainText, publicKey: state.rsa.publicKey };
    const [a, b] = await Promise.all([post('/api/rsa/encrypt', params), post('/api/rsa/encrypt', params)]);
    state.rsa.first = a.cipherText; state.rsa.second = b.cipherText; state.rsa.plainText = plainText;
    $('rsaDecCipher').value = a.cipherText;
    $('rsaDecKey').value = state.rsa.privateKey;
    $('rsaDecryptInputs').hidden = false;
    setEnabled('rsa', 'rsa-decrypt', true);
    setEnabled('rsa', 'rsa-other-key', true);
    render('rsa', '두 번 암호화', 'success', '같은 평문에서 다른 암호문이 나왔습니다',
      'OAEP 패딩의 임의성 때문에 실행할 때마다 암호문이 달라집니다.', root => {
        metric(root, [['평문 길이', bytes(plainText) + '바이트'], ['두 암호문', a.cipherText === b.cipherText ? '동일' : '서로 다름']]);
        label(root, '같은 공개키 · 같은 평문'); compare(root, ['암호문 1', a.cipherText], ['암호문 2', b.cipherText]);
        lesson(root, '암호문이 달라도 해당 키 쌍의 개인키로 둘 다 복호화할 수 있습니다.');
      }, { firstCipherText: a.cipherText, secondCipherText: b.cipherText });
  }
  async function rsaOtherKey() {
    if (!state.rsa || !state.rsa.first) throw new Error('먼저 암호화하세요.');
    const other = await api('/api/rsa/keys', { method: 'POST' });
    $('rsaDecKey').value = other.privateKey;
    render('rsa', '비교 키 적용', 'warning', '다른 키 쌍의 개인키를 입력했습니다',
      '개인키 입력란을 직접 수정하거나 원본으로 복원한 뒤 복호화해 보세요.', root => {
        lesson(root, '이 키는 방금 생성한 별도의 키 쌍에 속합니다. 실제 개인키는 입력하지 마세요.');
      }, { differentKeyGenerated: true });
  }
  async function rsaDecrypt() {
    if (!state.rsa || !state.rsa.first) throw new Error('먼저 암호화하세요.');
    const cipherText = textRequired('rsaDecCipher', '암호문', 10000).trim();
    const privateKey = textRequired('rsaDecKey', '개인키', 10000).trim();
    const altered = cipherText !== state.rsa.first || privateKey !== state.rsa.privateKey;
    try {
      const data = await post('/api/rsa/decrypt', { cipherText, privateKey });
      render('rsa', '복호화 성공', 'success', '평문을 복원했습니다', altered ? '입력한 암호문과 개인키로 복호화했습니다.' : '일치하는 개인키가 사용됐습니다.', root => {
        compare(root, ['원본', state.rsa.plainText], ['복호화', data.plainText]);
        lesson(root, '공개키만으로는 암호문을 복호화할 수 없습니다.');
      }, data);
      if (data.plainText === state.rsa.plainText) complete('rsa');
    } catch (error) {
      if (!altered || !error.status) throw error;
      render('rsa', '의도된 실패', 'warning', '입력한 값으로 복호화하지 못했습니다',
        '암호문 또는 개인키가 원본 실행 값과 다릅니다.', root => lesson(root, '키 쌍의 대응 관계와 암호문의 정확성을 확인하는 실험입니다.'), { expectedFailure: true, httpStatus: error.status });
    }
  }
  async function rsaLimit() {
    if (!state.rsa) throw new Error('먼저 키 쌍을 생성하세요.');
    const inputBytes = integerRange('rsaLimitLength', '실험할 바이트 수', 191, 500);
    try {
      await post('/api/rsa/encrypt', { plainText: 'A'.repeat(inputBytes), publicKey: state.rsa.publicKey });
      render('rsa', '예상과 다름', 'warning', inputBytes + '바이트 암호화에 성공했습니다',
        '현재 서버의 RSA 설정에서 처리되었습니다. 실제 키와 패딩 설정을 확인하세요.');
    } catch (error) {
      if (!error.status) throw error;
      render('rsa', '의도된 실패', 'warning', 'RSA 입력 크기 제한을 확인했습니다',
        inputBytes + '바이트 평문이 RSA-2048/OAEP-SHA256의 최대 크기 190바이트를 초과했습니다.', root => {
          metric(root, [['입력 길이', inputBytes + '바이트'], ['최대 길이', '190바이트']]);
          lesson(root, '큰 데이터는 AES로 처리하고 RSA는 작은 키 등을 암호화하는 데 사용합니다.');
        }, { expectedFailure: true, inputBytes, maxBytes: 190, httpStatus: error.status });
    }
  }
  async function hmacGenerate() {
    const message = textRequired('hmacMessage', '메시지', 5000);
    const secretKey = textRequired('hmacKey', '공유 비밀키', 500);
    const data = await post('/api/hmac/generate', { message, secretKey });
    state.hmac = { message, secretKey, signature: data.signature };
    $('hmacVerifyMessage').value = message;
    $('hmacVerifyKey').value = secretKey;
    $('hmacVerifySignature').value = data.signature;
    $('hmacVerifyInputs').hidden = false;
    setEnabled('hmac', 'hmac-verify', true);
    render('hmac', '인증값 생성', 'success', '메시지의 HMAC을 만들었습니다',
      '원본을 검증한 뒤 내용을 바꿔서 다시 검증해 보세요.', root => {
        code(root, 'HMAC-SHA256 인증값', data.signature);
        lesson(root, 'HMAC에는 송신자와 수신자가 함께 아는 비밀키가 필요합니다. 공개키 전자서명과는 다른 방식입니다.');
      }, data);
  }
  async function hmacVerify() {
    if (!state.hmac) throw new Error('먼저 인증값을 생성하세요.');
    const saved = state.hmac;
    const message = textRequired('hmacVerifyMessage', '받은 메시지', 5000);
    const secretKey = textRequired('hmacVerifyKey', '검증할 비밀키', 500);
    const signature = textRequired('hmacVerifySignature', '받은 인증값', 1000).trim();
    const changed = message !== saved.message || secretKey !== saved.secretKey || signature !== saved.signature;
    const data = await post('/api/hmac/verify', { message, secretKey, signature });
    render('hmac', data.valid ? '검증 성공' : '검증 실패', data.valid ? 'success' : 'warning',
      data.valid ? '입력한 메시지와 인증값이 일치합니다' : '입력한 메시지·키·인증값이 일치하지 않습니다',
      changed ? '수신 측의 입력을 변경해 검증했습니다.' : '생성할 때의 메시지와 비밀키를 사용했습니다.', root => {
        metric(root, [['비교 대상', changed ? '편집한 값' : '원본'], ['HMAC 검증', data.valid ? '일치' : '불일치']]);
        code(root, '검증한 메시지', message, false);
        lesson(root, '메시지의 무결성과 공유 비밀키를 알고 있는 상대가 만든 값인지 확인할 수 있습니다.');
      }, data);
    complete('hmac');
  }
  async function pbCompare() {
    const password = textRequired('pbPassword', '비밀번호', 500);
    const iterations = integerRange('pbIterations', '반복 횟수', 1000, 600000);
    const payload = { password, salt: '', iterations };
    const [a, b] = await Promise.all([post('/api/password/pbkdf2/generate', payload), post('/api/password/pbkdf2/generate', payload)]);
    state.pb = { password, encoded: a.encodedValue, first: a, second: b };
    $('pbVerifyPassword').value = password;
    $('pbEncoded').value = a.encodedValue;
    $('pbVerifyInputs').hidden = false;
    setEnabled('pbkdf2', 'pb-verify', true);
    render('pbkdf2', '생성 완료', 'success', '같은 비밀번호에서 다른 저장 값이 나왔습니다',
      '새 Salt를 사용해 각각 생성했습니다.', root => {
        metric(root, [['반복 횟수', number(iterations) + '회'], ['두 파생키', a.hashBase64 === b.hashBase64 ? '동일' : '서로 다름']]);
        label(root, 'Salt 비교'); compare(root, ['첫 번째 Salt', a.saltBase64], ['두 번째 Salt', b.saltBase64]);
        label(root, '파생키 비교'); compare(root, ['첫 번째 파생키', a.hashBase64], ['두 번째 파생키', b.hashBase64]);
        code(root, '첫 번째 저장 값', a.encodedValue);
        lesson(root, 'encodedValue에 알고리즘·반복 횟수·Salt·파생키가 담깁니다. 원문 비밀번호는 담기지 않습니다.');
      }, { first: a, second: b });
    complete('pbkdf2');
  }
  async function pbVerify() {
    if (!state.pb) throw new Error('먼저 저장 값을 생성하세요.');
    const password = textRequired('pbVerifyPassword', '검증할 비밀번호', 500);
    const encodedValue = textRequired('pbEncoded', '저장된 값', 5000).trim();
    const data = await post('/api/password/pbkdf2/verify', { password, encodedValue });
    render('pbkdf2', data.valid ? '비밀번호 일치' : '비밀번호 불일치', data.valid ? 'success' : 'warning',
      data.valid ? '입력한 비밀번호가 저장 값과 일치합니다' : '입력한 비밀번호가 저장 값과 일치하지 않습니다',
      '저장 값의 Salt와 반복 횟수를 이용해 다시 계산한 뒤 비교했습니다.', root => {
        metric(root, [['검증 결과', data.valid ? '일치' : '불일치'], ['저장된 원문', '없음']]);
        lesson(root, '비밀번호는 복호화하지 않습니다. 같은 방식으로 계산한 파생키를 저장 값과 비교합니다.');
      }, data);
  }
  function duration(ns) {
    const val = Number(ns);
    if (!Number.isFinite(val) || val < 0) return '측정 불가';
    const seconds = val / 1e9;
    return (seconds >= 1 ? seconds.toFixed(6) : seconds.toFixed(12)) + ' s';
  }
  function bars(root, rows) {
    const positives = rows.map(row => row[1]).filter(n => n > 0);
    const min = Math.min(...positives), max = Math.max(...positives);
    label(root, '알고리즘 처리 시간 · 1회 실행 평균');
    rows.forEach(([caption, nanos]) => {
      const line = node('div', 'bar-row');
      const track = node('div', 'bar-track');
      const bar = node('div', 'bar-fill');
      const position = max === min ? 55 : 10 + 90 * (Math.log10(Math.max(nanos, min)) - Math.log10(min)) / (Math.log10(max) - Math.log10(min));
      bar.style.width = (Number.isFinite(position) ? position : 10) + '%';
      track.append(bar);
      put(line, node('span', '', caption), track, node('strong', '', duration(nanos)));
      root.append(line);
    });
    root.append(node('div', 'bar-axis', '막대 길이는 로그 눈금입니다. 수치는 실제 평균 시간입니다.'));
  }
  function ratioSentence(target, baseline, targetNs, baselineNs) {
    if (!Number.isFinite(targetNs) || !Number.isFinite(baselineNs) || targetNs <= 0 || baselineNs <= 0) return '비율을 계산할 수 없습니다.';
    const ratio = targetNs / baselineNs;
    if (ratio > 0.95 && ratio < 1.05) return target + '와 ' + baseline + '의 측정 시간은 비슷합니다.';
    const factor = ratio >= 1 ? ratio : 1 / ratio;
    const formatted = new Intl.NumberFormat('ko-KR', { maximumFractionDigits: factor >= 100 ? 0 : 1, minimumFractionDigits: factor >= 100 ? 0 : 1 }).format(factor);
    return target + '는 ' + baseline + '보다 ' + formatted + '배 ' + (ratio >= 1 ? '느립니다.' : '빠릅니다.');
  }
  function ratios(root, lines) {
    label(root, '수치 비교');
    const wrap = node('div', 'ratio-list');
    lines.forEach(line => {
      const item = node('p', 'ratio-item');
      const match = line.match(/[\d,.]+배/);
      if (match) {
        item.append(document.createTextNode(line.slice(0, match.index)));
        item.append(node('strong', '', match[0]));
        item.append(document.createTextNode(line.slice(match.index + match[0].length)));
      } else item.textContent = line;
      wrap.append(item);
    });
    root.append(wrap);
  }
  async function benchmark(full) {
    const plainText = textRequired('benchPlain', '평문', 200);
    if (bytes(plainText) > 190) throw new Error('RSA 측정을 위해 평문을 UTF-8 기준 190바이트 이내로 입력하세요.');
    const repeatCount = integerRange('benchCount', '반복 횟수', 1, 100);
    const secretKey = textRequired('benchAesKey', 'AES 키', 500);
    let data, rows, subtitle;
    if (full) {
      const pbkdf2Iterations = integerRange('benchIterations', 'PBKDF2 반복', 1000, 100000);
      const hmacKey = textRequired('benchHmacKey', 'HMAC 키', 500);
      data = await post('/api/benchmark/full', { repeatCount, plainText, secretKey, hmacKey, pbkdf2Iterations });
      rows = [
        ['SHA-256', data.hash.sha256AverageNanos], ['SHA-512', data.hash.sha512AverageNanos],
        ['HMAC', data.hmac.generateAverageNanos], ['AES 암호화', data.aes.encryptAverageNanos],
        ['AES 복호화', data.aes.decryptAverageNanos], ['RSA 암호화', data.rsa.encryptAverageNanos],
        ['RSA 복호화', data.rsa.decryptAverageNanos], ['PBKDF2', data.pbkdf2.generateAverageNanos]
      ];
      subtitle = 'SHA·AES·RSA·HMAC·PBKDF2의 1회 평균을 비교했습니다.';
    } else {
      data = await post('/api/benchmark/aes-rsa', { repeatCount, plainText, secretKey });
      rows = [
        ['AES 암호화', data.aesEncryptAverageNanos], ['RSA 암호화', data.rsaEncryptAverageNanos],
        ['AES 복호화', data.aesDecryptAverageNanos], ['RSA 복호화', data.rsaDecryptAverageNanos]
      ];
      subtitle = '대칭키와 공개키 방식의 처리 시간을 비교했습니다.';
    }
    rows.sort((a, b) => a[1] - b[1]);
    render('benchmark', '측정 완료', 'success', full ? '알고리즘 5종 측정 완료' : 'AES와 RSA 측정 완료', subtitle, root => {
      metric(root, [['반복 횟수', number(repeatCount) + '회'], ['가장 빠른 항목', rows[0][0]]]);
      bars(root, rows);
      if (full) {
        ratios(root, [
          ratioSentence('RSA 암호화', 'AES 암호화', data.rsa.encryptAverageNanos, data.aes.encryptAverageNanos),
          ratioSentence('RSA 복호화', 'AES 복호화', data.rsa.decryptAverageNanos, data.aes.decryptAverageNanos),
          ratioSentence('PBKDF2 생성', 'SHA-256 해시', data.pbkdf2.generateAverageNanos, data.hash.sha256AverageNanos)
        ]);
        lesson(root, 'PBKDF2의 높은 계산 비용은 비밀번호를 오프라인으로 추측할 때 시도당 비용을 높입니다. 이는 서비스 안정성 지표가 아니며, 반복 횟수를 과도하게 높이면 로그인 서버의 부하도 증가합니다. RSA와 AES는 목적이 달라 속도만으로 보안 우열을 판단할 수 없습니다.');
      } else {
        ratios(root, [
          ratioSentence('RSA 암호화', 'AES 암호화', data.rsaEncryptAverageNanos, data.aesEncryptAverageNanos),
          ratioSentence('RSA 복호화', 'AES 복호화', data.rsaDecryptAverageNanos, data.aesDecryptAverageNanos)
        ]);
        lesson(root, 'AES는 데이터 암호화, RSA는 작은 키 보호에 쓰입니다. 전체 비교에서 비밀번호 저장용 PBKDF2도 확인해 보세요.');
      }
    }, data);
    complete('benchmark');
  }
  const actions = {
    hash, avalanche, bulk, dictionary, 'dictionary-clear': dictionaryClear,
    'dictionary-plain': dictionaryPlain, 'dictionary-lookup': dictionaryLookup,
    'aes-encrypt': () => aesEncrypt(false), 'aes-again': () => aesEncrypt(true),
    'aes-decrypt': aesDecrypt, 'rsa-keys': rsaKeys,
    'rsa-compare': rsaCompare, 'rsa-decrypt': rsaDecrypt,
    'rsa-other-key': rsaOtherKey, 'rsa-limit': rsaLimit,
    'hmac-generate': hmacGenerate, 'hmac-verify': hmacVerify,
    'pb-compare': pbCompare, 'pb-verify': pbVerify, 'bench-pair': () => benchmark(false),
    'bench-full': () => benchmark(true)
  };
  function preset(which) {
    switch (which) {
      case 'hash': $('hashPlain').value = 'password123'; $('hashSalt').value = 'ito-training-2026'; $('hashAlgo').value = 'SHA_256'; break;
      case 'av-one': $('avModified').value = value('avOriginal').slice(0, -1) + (value('avOriginal').endsWith('4') ? '5' : '4'); break;
      case 'av-same': $('avModified').value = value('avOriginal'); break;
      case 'rainbow-sample': $('rainbowPlain').value = 'password123'; $('rainbowSalt').value = ''; break;
      case 'rainbow-last':
        if (!state.hash) { toast('먼저 Hash 실습에서 비교를 실행하세요.'); return; }
        $('rainbowHash').value = state.hash; break;
      case 'aes-reset':
        if (!state.aes) { toast('먼저 암호화하세요.'); return; }
        $('aesDecCipher').value = state.aes.data.cipherText;
        $('aesDecIv').value = state.aes.data.ivBase64;
        $('aesDecKey').value = state.aes.input.secretKey; break;
      case 'aes-key-wrong':
        if (!state.aes) { toast('먼저 암호화하세요.'); return; }
        $('aesDecKey').value = state.aes.input.secretKey + '-other'; break;
      case 'rsa-reset':
        if (!state.rsa || !state.rsa.first) { toast('먼저 암호화하세요.'); return; }
        $('rsaDecCipher').value = state.rsa.first;
        $('rsaDecKey').value = state.rsa.privateKey; break;
      case 'hmac-reset':
        if (!state.hmac) { toast('먼저 인증값을 만드세요.'); return; }
        $('hmacVerifyMessage').value = state.hmac.message;
        $('hmacVerifyKey').value = state.hmac.secretKey;
        $('hmacVerifySignature').value = state.hmac.signature; break;
      case 'hmac-tamper':
        if (!state.hmac) { toast('먼저 인증값을 만드세요.'); return; }
        $('hmacVerifyMessage').value = state.hmac.message.includes('amount=10000')
          ? state.hmac.message.replace('amount=10000', 'amount=90000') : state.hmac.message + '&changed=true'; break;
      case 'hmac-key-wrong':
        if (!state.hmac) { toast('먼저 인증값을 만드세요.'); return; }
        $('hmacVerifyKey').value = state.hmac.secretKey + '-other'; break;
      case 'pb-reset':
        if (!state.pb) { toast('먼저 저장 값을 생성하세요.'); return; }
        $('pbVerifyPassword').value = state.pb.password;
        $('pbEncoded').value = state.pb.encoded; break;
      case 'pb-wrong':
        if (!state.pb) { toast('먼저 저장 값을 생성하세요.'); return; }
        $('pbVerifyPassword').value = state.pb.password + '-other'; break;
      default: return;
    }
    toast('입력값을 변경했습니다.');
  }
  function init() {
    loadProgress();
    makeNavigation();
    ids.forEach(empty);
    document.querySelectorAll('[data-action]').forEach(button => state.actions[button.dataset.action] = !button.disabled);
    updateProgress();
    document.addEventListener('click', async event => {
      const copyButton = event.target.closest('[data-copy]');
      if (copyButton) {
        try { await navigator.clipboard.writeText(copyButton.dataset.copy); toast('값을 복사했습니다.'); }
        catch (_) { toast('클립보드 복사 권한을 확인하세요.'); }
        return;
      }
      const go = event.target.closest('[data-go]');
      if (go) { event.preventDefault(); navigate(go.dataset.go); return; }
      const pre = event.target.closest('[data-preset]');
      if (pre) { preset(pre.dataset.preset); return; }
      const button = event.target.closest('[data-action]');
      if (button && !button.disabled) {
        const page = button.closest('.lab-page');
        if (page) run(page.id.slice(5), actions[button.dataset.action]);
      }
    });
    $('aesMode').addEventListener('change', () => $('aesSeedWrap').hidden = value('aesMode') !== 'DERIVED');
    document.querySelectorAll('input,textarea').forEach(input => input.addEventListener('input', () => clearInvalid(input.id)));
    $('menuButton').addEventListener('click', () => {
      const open = !$('sidebar').classList.contains('open');
      $('sidebar').classList.toggle('open', open); $('sidebarScrim').hidden = !open;
      $('menuButton').setAttribute('aria-expanded', String(open));
    });
    $('sidebarScrim').addEventListener('click', closeMenu);
    $('resetProgress').addEventListener('click', () => {
      if (!window.confirm('완료 표시와 이어서 보기 위치를 초기화할까요?')) return;
      state.done.clear(); state.last = 'hash'; saveProgress(); updateProgress(); toast('진행 기록을 초기화했습니다.');
    });
    try { document.documentElement.dataset.theme = localStorage.getItem('crypto-lab:v2:theme') === 'dark' ? 'dark' : 'light'; } catch (_) {}
    $('themeButton').setAttribute('aria-label', document.documentElement.dataset.theme === 'dark' ? '밝은 테마로 변경' : '어두운 테마로 변경');
    $('themeButton').addEventListener('click', () => {
      const next = document.documentElement.dataset.theme === 'dark' ? 'light' : 'dark';
      document.documentElement.dataset.theme = next;
      $('themeButton').setAttribute('aria-label', next === 'dark' ? '밝은 테마로 변경' : '어두운 테마로 변경');
      try { localStorage.setItem('crypto-lab:v2:theme', next); } catch (_) {}
    });
    window.addEventListener('popstate', () => navigate(location.hash.slice(1) || 'home', false));
    window.addEventListener('hashchange', () => navigate(location.hash.slice(1) || 'home', false));
    navigate(location.hash.slice(1) || 'home', false);
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
