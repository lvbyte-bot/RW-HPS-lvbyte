# RW-HPS - Http API

> Note:
> - This chapter introduces an in-built plugin in `RW-HPS`.
> - This chapter is **not** about the `UPLIST-API`.

## Usage

To use the HttpApi, you first need to make a `GET` request to `/HttpApi/api/AuthCookie` with the parameter `passwd`.

The default password is *randomly generated*. You can find it in the configuration file under `webToken`.

After the request is completed, you will receive a cookie named `HttpApi-Authentication`, which is valid for **24 hours**.

Each subsequent request **must** carry this cookie, otherwise, the server will return a 403 error.

### GET

Path: `/HttpApi/api/get/xxx`

#### info

##### SystemInfo

Path: `/HttpApi/api/get/info/SystemInfo` (similarly for other endpoints)

Response:

```json
{
    "status": "OK",
    "data": "{\"system\":\"Linux\",\"arch\":\"amd64\",\"jvmName\":\"OpenJDK 64-Bit Server VM\",\"jvmVersion\":\"11.0.20\"}"
}
```

##### GameInfo

Response:

```json
{
    "status": "OK",
    "data": "{\"income\":1.0,\"noNukes\":false,\"credits\":0,\"sharedControl\":false,\"players\":[]}"
}
```

##### ModsInfo

Response:

```json
{
    "status": "OK",
    "data": "[\"RW-HPS CoreUnits\"]"
}
```

#### event

##### GameOver

Response:

```json
{
    "status": "OK",
    "data": "[]"
}
```

### POST

Path: `/HttpApi/api/post/xxx`

#### run

##### ServerCommand

Purpose: *Execute server commands and return the result*

// TODO

##### ClientCommand

Purpose: *Execute client commands and return the result*

// TODO

### WebSocket

Path: `/WebSocket/HttpApi/api/Console`

The client needs to `ping` every few seconds (method below). If there is no activity within 10 seconds, the server will disconnect the client due to timeout.

The following operations are available:

#### Register

Purpose: *Register this cookie with the server. Use the cookie obtained above as the parameter. After registration, the client no longer needs to carry this cookie.*

Send:

```json
{
    "cookie": "5420dedf8f1829bc43d03843d26216523fe19e06ee253abcacd3a4ee5b9af12b",
    "type": "register"
}
```

Response:

```json
{
    "code": 200,
    "data": "Register OK"
}
```

#### Ping

Purpose: *Keep alive*

Send:

```json
{
    "type": "ping"
}
```

Response:

```json
{
    "code": 200,
    "data": "pong"
}
```

#### GetConsole

Purpose: *Send the server console input to WebSocket until the client disconnects*

Send:

```json
{
    "type": "getConsole"
}
```

Response:

```json
{
    "code": 200,
    "data": "[9072-79-85 20:08:33] This is a non-existent log entry"
}
```

#### RunCommand

Purpose: *Run server commands*

Note: To get the result, use `getConsole` first.

Send:

```json
{
    "type": "runCommand",
    "runCommand": "version"
}
```

Response:

**None**