package com.binance.dex.api.client.impl;

import com.binance.dex.api.client.Wallet;
import com.binance.dex.api.client.domain.TransactionMetadata;
import com.binance.dex.api.client.domain.broadcast.TransactionOption;
import com.binance.dex.api.client.domain.broadcast.Transfer;
import com.binance.dex.api.client.SignCallBack;
import com.binance.dex.api.client.encoding.message.TransactionRequestAssembler;
import com.binance.dex.api.client.encoding.message.TransactionRequestCustomAssembler;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

/**
 * @author wangke
 * @description: TODO
 * @date 2019-05-23 01:16
 */
public class BinanceDexApiNodeCustomClient extends BinanceDexApiNodeClientImpl {

    public BinanceDexApiNodeCustomClient(String nodeUrl, String hrp) {
        super(nodeUrl, hrp);
    }

    public List<TransactionMetadata> Broadcast(String requestBody, Wallet wallet, boolean sync) {
        return sync ? syncBroadcast(requestBody, wallet) : asyncBroadcast(requestBody, wallet);
    }


    @Override
    public List<TransactionMetadata> transfer(Transfer transfer, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestCustomAssembler assembler = new TransactionRequestCustomAssembler(wallet, options);
        String requestPayload = "0x" + assembler.buildTransferPayload(transfer);
        if (sync) {
            return syncBroadcast(requestPayload, wallet);
        } else {
            return asyncBroadcast(requestPayload, wallet);
        }
    }
}
