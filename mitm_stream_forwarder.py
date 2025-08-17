# sse_only.py
from mitmproxy import ctx, http

class SSELogger:
    def __init__(self):
        self.active_flow = None

    def responseheaders(self, flow: http.HTTPFlow):
        # Accept 헤더가 SSE인 요청만 처리
        if flow.request.headers.get("Accept", "").startswith("text/event-stream"):
            self.active_flow = flow
            ctx.log.info(f"▶▶▶ SSE START: {flow.request.url}")

    def responsechunk(self, flow: http.HTTPFlow, chunk: bytes):
        if flow is self.active_flow:
            try:
                text = chunk.decode("utf-8", errors="ignore")
            except:
                text = repr(chunk)
            # 빈 줄 제외하고, 수신된 라인별로 바로바로 찍어 줌
            for line in text.splitlines():
                if line.strip():
                    ctx.log.info(f"SSE ▶ {line}")

    def done(self):
        if self.active_flow:
            ctx.log.info("◀◀◀ SSE END")
            self.active_flow = None

addons = [
    SSELogger()
]
