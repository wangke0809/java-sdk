package com.binance.dex.api.client;

/**
 * @author wangke
 * @description: 签名回调函数
 * @date 2019-05-23 00:55
 */
public interface SignCallBack {
    byte[] sign(byte[] msgHash);
}
