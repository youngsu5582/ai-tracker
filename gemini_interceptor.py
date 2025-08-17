# gemini_interceptor.py
import json
import requests
from mitmproxy import http

LOGGING_SERVER_URL = "http://localhost:8080/api/v1/proxy/requests"

class GeminiInterceptor:
    def request(self, flow: http.HTTPFlow) -> None:
        # Filter for requests to the Gemini API
        if "generativelanguage.googleapis.com" in flow.request.host:
            print(f"Intercepted Gemini API request to: {flow.request.pretty_url}")

            # Prepare data to be sent to the logging server
            data = {
                "uri": flow.request.path,
                "method": flow.request.method,
                "body": json.loads(flow.request.get_text() or '{}') # Ensure body is valid JSON
            }

            # Send the intercepted data to the Spring Boot backend
            try:
                response = requests.post(LOGGING_SERVER_URL, json=data, headers={'Content-Type': 'application/json'})
                print(f"Sent data to logging server. Status: {response.status_code}")
            except requests.exceptions.RequestException as e:
                print(f"Error sending data to logging server: {e}")

addons = [
    GeminiInterceptor()
]