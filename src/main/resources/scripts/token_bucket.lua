local key = KEYS[1]

local capacity = tonumber(ARGV[1])
local refillRate = tonumber(ARGV[2])
local currentTime = tonumber(ARGV[3])

local tokens = tonumber(redis.call('HGET', key, 'tokens'))
local lastRefillTime = tonumber(redis.call('HGET', key, 'lastRefillTime'))

if tokens == nil then
    tokens = capacity
    lastRefillTime = currentTime
end

local elapsedTime = (currentTime - lastRefillTime) / 1000.0

local tokensToAdd = elapsedTime * refillRate

tokens = math.min(capacity, tokens + tokensToAdd)

lastRefillTime = currentTime

if tokens < 1 then
    redis.call(
            'HSET',
            key,
            'tokens',
            tokens,
            'lastRefillTime',
            lastRefillTime
    )
    return 0
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

return 1