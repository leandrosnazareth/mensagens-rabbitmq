RabbitMQ flow (recommended)

Goal
- Deliver SMS sending requests to a queue and wait for the provider to call back the HTTP endpoint when a response/receipt is available.

Suggested message contract
- Exchange: direct exchange (or default)
- Queue: sms.requests
- Message body: JSON with fields {"id":"<string>","mensagem":"<string>","metadata":{...}}
- Headers:
  - correlationId: UUID to link request->response
  - replyTo: optional; we use HTTP callback instead of rabbit reply queue in this design

Design notes
1) Sender (this application)
- Creates a new id (UUID) for the SMS request.
- Publishes the message to the `sms.requests` queue with header `correlationId=id` and any metadata.
- Creates a CompletableFuture and stores it in a map keyed by id (or stores state in Redis for persistence).
- Waits on the CompletableFuture up to `timeout.delay.ms`. If completed: return result; otherwise return HTTP 202 Accepted or timeout error.

2) Consumer (external SMS provider)
- Listens to `sms.requests` queue and processes each message.
- After receiving SMS delivery/response from the end user, the provider calls the HTTP callback of this application:
  POST /callback/sms
  Body: { "id": "<correlationId>", "mensagem": "<reply text>" }
  Header: X-Callback-Token: <token> (optional - recommended)
- The provider MAY also call an HTTP endpoint to acknowledge acceptance; but final response must use the callback above.

3) This application callback endpoint
- Validates optional token header against `callback.token` property
- Looks up the pending CompletableFuture by id and completes it with the payload
- If no pending request exists, returns 404 (the provider should persist attempts or call other API)

Correlation and reliability
- For increased reliability use Redis or DB to persist pending requests so a crash does not lose them.
- Add dead-letter queue (DLQ) for unprocessed messages.
- Use correlationId headers so consumers can map to requests easily.

Security
- Protect the callback endpoint with a token (X-Callback-Token) or HMAC signature.
- Use HTTPS for callbacks and RabbitMQ TLS in production.

Observability
- Emit metrics for pending requests, time-to-reply, timeouts, and queue depth.
- Add logs including correlationId for tracing.


Notes on current repo
- The local `SmsListener` previously simulated the consumer. It was removed to avoid auto-completing requests in dev.
- You can implement a test consumer to emulate provider behavior that calls /callback/sms after processing.
