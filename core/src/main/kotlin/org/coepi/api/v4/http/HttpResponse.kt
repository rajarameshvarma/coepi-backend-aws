package org.coepi.api.v4.http

import java.nio.ByteBuffer

sealed class HttpResponse(val status: Int, val body: ByteBuffer?)

class Ok(body: ByteBuffer?) : HttpResponse(200, body) {
    constructor() : this(null)
}

class BadRequest(body: ByteBuffer?) : HttpResponse(400, body) {
    constructor(body: String) : this(ByteBuffer.wrap(body.toByteArray()))
}

class Unauthorized(body: ByteBuffer?) : HttpResponse(401, body) {
    constructor(body: String) : this(ByteBuffer.wrap(body.toByteArray()))
}
