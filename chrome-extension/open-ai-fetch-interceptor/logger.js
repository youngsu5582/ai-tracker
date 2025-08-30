// chrome-extension/src/logger.js

/**
 * Simple logging utility that respects a debug flag.
 * Logs messages to the console only if debugging is enabled.
 */
const Logger = {
  debugEnabled: false,

  /**
   * Initializes the logger with the debug status.
   * @param {boolean} enabled - True to enable debug logging, false otherwise.
   */
  init: function (enabled) {
    this.debugEnabled = enabled;
  },

  create: function (prefixInput) {
    const context = prefixInput || getCallerFileName();
    const prefix = `[${context}]`;
    return {
      debug: (...args) => {
        if (this.debugEnabled) {
          console.log(prefix, "[Debug]", ...args);
        }
      },
      info: (...args) => {
        console.log(prefix, "[Info]", ...args);
      },
      warn: (...args) => {
        console.warn(prefix, "[Warn]", ...args);
      },
      error: (...args) => {
        console.error(prefix, "[Error]", ...args);
      }
    };
  },
};

function getCallerFileName() {
  try {
    throw new Error();
  } catch
      (e) {
    // 스택 트레이스 문자열을 줄 단위로 나눕니다.
    const stackLines = e.stack.split('\n');

    // 스택 라인 예시 (브라우저마다 약간 다를 수 있음):
    // "at getCallerFileName (file:///.../logger.js:10:11)"
    // "at Object.create (file:///.../logger.js:25:32)"
    // "at file:///.../fetch_interceptor.js:4:18"

    // 2번 인덱스가 보통 이 함수(getCallerFileName)를 호출한 곳의 정보입니다.
    // 하지만 create 함수를 통해 호출되므로 3번 인덱스를 확인합니다.
    const callerLine = stackLines[3] || '';

    // 정규표현식을 사용하여 파일 경로에서 파일명만 추출합니다.
    // 마지막 '/'와 ':' 사이의 문자열을 찾습니다.
    const match = callerLine.match(/\/([^/:]+):\d+:\d+/);

    if (match && match[1]) {
      return match[1]; // 첫 번째 그룹이 파일명입니다.
    }
  }
  return 'unknown'; // 파일명을 찾지 못한 경우
}