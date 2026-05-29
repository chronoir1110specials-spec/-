package com.ruoyi.agent.core;

import java.util.concurrent.TimeUnit;
import com.ruoyi.common.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 调用限流服务。
 *
 * <p>基于 Redis 的"每用户每日调用次数"限流。计数 key 形如
 * {@code rate_limit:{date}:{userId}}，当天 0 点后过期；阈值存于
 * {@code rate_limit:daily_limit}，可由管理员动态调整。</p>
 *
 * @author ruoyi
 */
@Service
public class RateLimitService
{
    /** 每日限额配置 key */
    private static final String LIMIT_CONFIG_KEY = "rate_limit:daily_limit";

    /** 计数 key 前缀 */
    private static final String COUNTER_PREFIX = "rate_limit:counter:";

    /** 默认每日调用上限 */
    private static final int DEFAULT_DAILY_LIMIT = 100;

    @Autowired
    private RedisService redisService;

    /**
     * 获取当前每日限额。
     *
     * @return 每日限额（<=0 表示不限流）
     */
    public int getDailyLimit()
    {
        Integer limit = redisService.getCacheObject(LIMIT_CONFIG_KEY);
        return limit == null ? DEFAULT_DAILY_LIMIT : limit;
    }

    /**
     * 设置每日限额。
     *
     * @param limit 每日限额，<=0 表示不限流
     */
    public void setDailyLimit(int limit)
    {
        redisService.setCacheObject(LIMIT_CONFIG_KEY, limit);
    }

    /**
     * 校验并递增某用户当日计数。
     *
     * @param userId 用户 ID
     * @return true 表示允许调用；false 表示已超出当日上限
     */
    public boolean tryAcquire(Long userId)
    {
        if (userId == null)
        {
            return true;
        }
        int limit = getDailyLimit();
        if (limit <= 0)
        {
            return true;
        }
        String key = COUNTER_PREFIX + today() + ":" + userId;
        Long current = redisService.redisTemplate.opsForValue().increment(key);
        if (current != null && current == 1L)
        {
            // 首次计数，设置当日过期（粗略 24h，足够演示）
            redisService.expire(key, 24, TimeUnit.HOURS);
        }
        return current == null || current <= limit;
    }

    /**
     * 查询某用户当日已用次数。
     *
     * @param userId 用户 ID
     * @return 已用次数
     */
    public long getUsed(Long userId)
    {
        if (userId == null)
        {
            return 0L;
        }
        String key = COUNTER_PREFIX + today() + ":" + userId;
        if (!redisService.hasKey(key))
        {
            return 0L;
        }
        // 计数键由 increment 写入（裸数字），用 increment(key,0) 读当前值，避免 JSON 反序列化路径
        Long used = redisService.redisTemplate.opsForValue().increment(key, 0L);
        return used == null ? 0L : used;
    }

    private String today()
    {
        java.time.LocalDate d = java.time.LocalDate.now();
        return d.toString();
    }
}
