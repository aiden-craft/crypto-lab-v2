/* 각 실습의 별도 교육용 Java 예제를 읽기 전용 코드 블록으로 보여줍니다. */
(() => {
  'use strict';
  const examples = {
    hash: {
      file: 'HashSaltExample.java', title: 'Hash와 Salt',
      focus: 'MessageDigest에 Salt와 평문을 넣고 SHA-256 결과를 비교합니다.',
      note: '출력 길이는 항상 256비트입니다. 비밀번호 저장은 PBKDF2 예제를 참고하세요.'
    },
    avalanche: {
      file: 'AvalancheExample.java', title: 'Avalanche Effect',
      focus: '두 해시 바이트를 XOR하고 Integer.bitCount로 바뀐 비트를 셉니다.',
      note: '16진수 문자의 차이 개수가 아니라 전체 비트 수 대비 변화율을 계산합니다.'
    },
    bulk: {
      file: 'BulkHashExample.java', title: 'Bulk Hash',
      focus: '입력 중복과 서로 다른 입력 사이의 해시 충돌을 분리해 집계합니다.',
      note: '관찰된 충돌이 없어도 충돌이 불가능하다는 증거는 아닙니다.'
    },
    rainbow: {
      file: 'DictionaryAttackExample.java', title: '사전 공격 실험',
      focus: '후보의 해시를 미리 계산하고 대상 해시와 대조합니다.',
      note: '복호화나 해시 체인 방식의 Rainbow Table이 아닌 간단한 사전 대조 예제입니다.'
    },
    aes: {
      file: 'AesGcmExample.java', title: 'AES 암호화',
      focus: 'AES-GCM으로 암호화하고 매번 새 nonce를 생성해 복호화합니다.',
      note: '실습 화면은 IV 차이를 보기 위해 AES-CBC를 사용합니다. 이 별도 예제는 인증 기능이 있는 AES-GCM을 사용하므로 모드와 결과 형식이 다릅니다.'
    },
    rsa: {
      file: 'RsaOaepExample.java', title: 'RSA 공개키 암호화',
      focus: 'RSA-2048 키 쌍과 OAEP-SHA256으로 짧은 데이터를 암복호화합니다.',
      note: '같은 평문을 두 번 암호화해 암호문이 달라지는 것을 확인할 수 있습니다.'
    },
    hmac: {
      file: 'HmacExample.java', title: 'HMAC 무결성 검증',
      focus: '공유 키로 HMAC-SHA256을 만들고 일정 시간 비교 함수로 검증합니다.',
      note: '메시지가 달라지면 기존 인증값으로 검증할 수 없습니다.'
    },
    pbkdf2: {
      file: 'Pbkdf2Example.java', title: 'PBKDF2 비밀번호 저장',
      focus: '임의 Salt와 반복 횟수로 파생키를 만들고 저장 값을 다시 검증합니다.',
      note: '저장 값에 알고리즘·반복 횟수·Salt·파생키를 포함하고 원문은 저장하지 않습니다.'
    },
    benchmark: {
      file: 'BenchmarkExample.java', title: '알고리즘 Benchmark',
      focus: '예열 후 nanoTime으로 평균 초 단위 시간을 측정하고 배수를 계산합니다.',
      note: 'SHA-256과 PBKDF2로 측정 패턴을 보여줍니다. 지연 시간의 차이가 보안 강도의 순위는 아닙니다.'
    }
  };
  const $ = id => document.getElementById(id);
  const cache = new Map();
  let currentCode = '';
  let activeRequest = 0;
  let opener = null;
  const tokens = /(\/\/.*|\/\*.*?\*\/|"(?:\\.|[^"\\])*"|'(?:\\.|[^'\\])*'|@\w+|\b(?:public|private|protected|class|record|interface|implements|return|new|if|else|for|while|try|catch|finally|throw|throws|final|static|void|int|long|double|boolean|byte|char|String|null|true|false)\b)/g;

  function highlight(text, target) {
    tokens.lastIndex = 0;
    let from = 0;
    for (const match of text.matchAll(tokens)) {
      if (match.index > from) target.append(document.createTextNode(text.slice(from, match.index)));
      const token = document.createElement('span');
      token.className = match[0].startsWith('//') || match[0].startsWith('/*')
        ? 'syntax-comment' : /^["']/.test(match[0]) ? 'syntax-string'
          : match[0].startsWith('@') ? 'syntax-annotation' : 'syntax-keyword';
      token.textContent = match[0];
      target.append(token);
      from = match.index + match[0].length;
    }
    target.append(document.createTextNode(text.slice(from)));
  }

  function render(code) {
    currentCode = code;
    const fragment = document.createDocumentFragment();
    code.replace(/\n$/, '').split('\n').forEach((line, index) => {
      const row = document.createElement('div');
      row.className = 'example-line';
      const number = document.createElement('span');
      number.className = 'example-line-number';
      number.textContent = String(index + 1);
      const content = document.createElement('span');
      content.className = 'example-line-code';
      highlight(line, content);
      row.append(number, content);
      fragment.append(row);
    });
    $('exampleLines').replaceChildren(fragment);
    $('exampleStatus').textContent = '';
    $('exampleCopy').disabled = false;
    $('exampleViewport').scrollTop = 0;
    $('exampleViewport').scrollLeft = 0;
  }

  async function openExample(button) {
    const info = examples[button.dataset.example];
    if (!info) return;
    opener = button;
    const dialog = $('exampleDialog');
    $('exampleTitle').textContent = info.title + ' · Java 예시 코드';
    $('exampleFocus').textContent = info.focus;
    $('exampleNote').textContent = info.note;
    $('exampleFilename').textContent = info.file;
    $('exampleLines').replaceChildren();
    $('exampleStatus').textContent = '예시 코드를 불러오는 중…';
    $('exampleCopy').disabled = true;
    currentCode = '';
    const request = ++activeRequest;
    dialog.showModal();
    try {
      if (!cache.has(info.file)) {
        const response = await fetch('/examples/' + info.file, { headers: { Accept: 'text/plain' } });
        if (!response.ok) throw new Error('HTTP ' + response.status);
        cache.set(info.file, await response.text());
      }
      if (request === activeRequest && dialog.open) render(cache.get(info.file));
    } catch (_) {
      if (request === activeRequest) {
        $('exampleStatus').textContent = '코드를 불러오지 못했습니다. 서버 실행 상태를 확인하고 다시 열어주세요.';
      }
    }
  }

  function init() {
    const dialog = $('exampleDialog');
    document.querySelectorAll('[data-example]').forEach(button =>
      button.addEventListener('click', () => openExample(button)));
    $('exampleClose').addEventListener('click', () => dialog.close());
    $('exampleCopy').addEventListener('click', async () => {
      if (!currentCode) return;
      try {
        await navigator.clipboard.writeText(currentCode);
        $('exampleStatus').textContent = '예시 코드를 복사했습니다.';
      } catch (_) {
        $('exampleStatus').textContent = '클립보드 권한을 확인하세요.';
      }
    });
    dialog.addEventListener('close', () => {
      activeRequest++;
      if (opener) opener.focus();
    });
  }
  if (document.readyState === 'loading') document.addEventListener('DOMContentLoaded', init);
  else init();
})();
