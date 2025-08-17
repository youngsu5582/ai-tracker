# debug_sse.py
from mitmproxy import ctx, http

class DebugSSELogger:
    def __init__(self):
        # 캡처할 도메인 리스트
        self.domains = ["api.gemini.com", "chatgpt.com", "openai.com"]
        self.active_flows = set()

    def responseheaders(self, flow: http.HTTPFlow):
        host = flow.request.host
        accept = flow.request.headers.get("Accept", "")
        # 디버그용 로그
        ctx.log.info(f"[DBG] Host={host}, Accept={accept}")
        # 도메인 필터 + Accept에 text/event-stream 포함 여부
        if any(d in host for d in self.domains) and "text/event-stream" in accept:
            self.active_flows.add(flow.id)
            ctx.log.info(f"▶▶▶ SSE START: {flow.request.url}")

    def responsechunk(self, flow: http.HTTPFlow, chunk: bytes):
        # 활성화된 flow만 처리
        if flow.id not in self.active_flows:
            return
        text = chunk.decode("utf-8", errors="ignore")
        for line in text.splitlines():
            line = line.strip()
            if line:
                ctx.log.info(f"SSE ▶ {line}")

    def done(self):
        # flow가 끝나면 클리어
        for fid in list(self.active_flows):
            ctx.log.info("◀◀◀ SSE END")
            self.active_flows.remove(fid)

addons = [ DebugSSELogger() ]
