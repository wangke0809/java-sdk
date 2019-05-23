package com.binance.dex.api.client.encoding.message;

import com.binance.dex.api.client.Wallet;
import com.binance.dex.api.client.domain.broadcast.TransactionOption;
import com.binance.dex.api.client.domain.broadcast.Transfer;
import com.binance.dex.api.client.encoding.Crypto;
import com.binance.dex.api.client.encoding.EncodeUtils;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * @author wangke
 * @description: TODO
 * @date 2019-05-23 01:29
 */
public class TransactionRequestCustomAssembler extends TransactionRequestAssembler {

    private Wallet wallet;
    private TransactionOption options;

    public TransactionRequestCustomAssembler(Wallet wallet, TransactionOption option){
        super(wallet, option);
        this.wallet = wallet;
        this.options = option;
    }

    @Override
    public String buildTransferPayload(Transfer transfer)
            throws IOException, NoSuchAlgorithmException {
        TransferMessage msgBean = createTransferMessage(transfer);
        byte[] msg = encodeTransferMessage(msgBean);
        byte[] signature = encodeSignature(sign(msgBean));
        byte[] stdTx = encodeStdTx(msg, signature);
        return EncodeUtils.bytesToHex(stdTx);
    }

    @Override
    byte[] sign(BinanceDexTransactionMessage msg)
            throws NoSuchAlgorithmException, IOException {
        SignData sd = new SignData();
        sd.setChainId(wallet.getChainId());
        sd.setAccountNumber(String.valueOf(wallet.getAccountNumber()));
        sd.setSequence(String.valueOf(wallet.getSequence()));
        sd.setMsgs(new BinanceDexTransactionMessage[]{msg});

        sd.setMemo(options.getMemo());
        sd.setSource(String.valueOf(options.getSource()));
        sd.setData(options.getData());
        if (wallet.getEcKey() == null && wallet.getLedgerKey() != null) {
            return Crypto.sign(EncodeUtils.toJsonEncodeBytes(sd), wallet.getLedgerKey());
        }

        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] msgHash = digest.digest(EncodeUtils.toJsonEncodeBytes(sd));

        return wallet.getSignCallBack().sign(msgHash);
    }
}
