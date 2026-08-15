--[[
We are using this lua script for the execution of rate limiting with Redis. It guarantees the absolute atomicity and
eliminate race conditions in the distributed system.
]]
local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local currentTime = tonumber(ARGV[3])
local ttl = tonumber(ARGV[4])

local tokens = redis.call('HGET', key, 'tokens')
local lastRefillTime = redis.call('HGET', key, 'lastRefillTime')

if tokens == false then
    tokens = capacity
    lastRefillTime = currentTime
else
    tokens = tonumber(tokens)
    lastRefillTime = tonumber(lastRefillTime)

    local elapsedTime =
    (currentTime - lastRefillTime) / 1000.0

    local tokensToAdd =
    elapsedTime * refillRate

    tokens = math.min(
            capacity,
            tokens + tokensToAdd
    )

    lastRefillTime = currentTime
end

if tokens < 1 then

    local retryAfter =
    math.ceil((1 - tokens) / refillRate)

    redis.call(
            'HSET',
            key,
            'tokens',
            tokens,
            'lastRefillTime',
            lastRefillTime
    )

    redis.call(
            'EXPIRE',
            key,
            ttl
    )

    return "0|" ..
            math.floor(tokens) ..
            "|" ..
            retryAfter
end

tokens = tokens - 1

redis.call(
        'HSET',
        key,
        'tokens',
        tokens,
        'lastRefillTime',
        lastRefillTime
)

redis.call(
        'EXPIRE',
        key,
        ttl
)

return "1|" ..
        math.floor(tokens) ..
        "|0"