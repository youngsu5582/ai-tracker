# debug_transparent_v2.py
from mitmproxy import ctx, http, tcp, connection

class DetailedDebugger:
    # clientconnect 이벤트의 타입 힌트를 범용적인 타입으로 수정하여 버전 호환성 확보
    def clientconnect(self, client: connection.Client):
        ctx.log.info(f"[DEBUG] Client connected: {client.peername}")

    def tcp_start(self, flow: tcp.TCPFlow):
        ctx.log.info(f"[DEBUG] TCP Start: Client {flow.client_conn.peername} -> Server {flow.server_conn.address}")

    def request(self, flow: http.HTTPFlow):
        original_dst = getattr(flow.server_conn, 'original_dst', None)
        ctx.log.info(f"[DEBUG] Request received from {flow.client_conn.peername}")
        ctx.log.info(f"  ┣━ Intended destination: {flow.request.host}:{flow.request.port}")
        ctx.log.info(f"  ┗━ Original destination from socket: {original_dst}")
        if not original_dst:
            ctx.log.error("  ┗━ ❌ ERROR: Original destination info is MISSING!")

    def response(self, flow: http.HTTPFlow):
        ctx.log.info(f"[DEBUG] Response for {flow.request.pretty_url} -> {flow.response.status_code}")

    def error(self, flow: http.HTTPFlow):
        ctx.log.error(f"[DEBUG] Flow Error: {flow.error}")

addons = [
    DetailedDebugger()
]
