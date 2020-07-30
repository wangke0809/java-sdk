package com.binance.dex.api.client.encoding.message;

import com.binance.dex.api.client.BinanceDexEnvironment;
import com.binance.dex.api.client.Wallet;
import com.binance.dex.api.client.domain.TransactionMetadata;
import com.binance.dex.api.client.domain.broadcast.Transaction;
import com.binance.dex.api.client.domain.broadcast.TransactionOption;
import com.binance.dex.api.client.domain.broadcast.Transfer;
import com.binance.dex.api.client.encoding.Crypto;
import com.binance.dex.api.client.impl.BinanceDexApiNodeCustomClient;
import org.bitcoinj.core.ECKey;
import org.bitcoinj.core.Sha256Hash;
import org.bitcoinj.core.Utils;
import org.junit.Ignore;
import org.junit.Test;

import java.math.BigInteger;
import java.util.Arrays;
import java.util.List;

/**
 * @author wangke
 * @description: 测试签名回调
 * @date 2019-05-23 01:11
 */
@Ignore("Manual run only")
public class SignCallBackTest {
    BinanceDexApiNodeCustomClient binanceDexNodeApi = new BinanceDexApiNodeCustomClient(BinanceDexEnvironment.TEST_NET.getNodeUrl(), BinanceDexEnvironment.TEST_NET.getHrp());

    @Test
    public void test() throws Exception{



        List<String> words =
                Arrays.asList("stairs merit soon tenant bamboo wool tenant job local moral human warm box oil bind prosper organ exchange mother ship suggest hint elegant siren".split(" "));

        String privateKey = Crypto.getPrivateKeyFromMnemonicCode(words);
        ECKey ecKey = ECKey.fromPrivate(new BigInteger(privateKey, 16));
        System.out.println(ecKey.getPublicKeyAsHex());
        System.out.println(ecKey.decompress().getPublicKeyAsHex());


//        Wallet wallet = Wallet.createWalletFromMnemonicCode(words, BinanceDexEnvironment.TEST_NET);
        Wallet wallet = new Wallet(ecKey.decompress().getPublicKeyAsHex(),
                BinanceDexEnvironment.TEST_NET,
                (byte[] msgHash)->{
                    ECKey.ECDSASignature signature = ecKey.sign(Sha256Hash.wrap(msgHash));

                    byte[] result = new byte[64];
                    System.arraycopy(Utils.bigIntegerToBytes(signature.r, 32), 0, result, 0, 32);
                    System.arraycopy(Utils.bigIntegerToBytes(signature.s, 32), 0, result, 32, 32);
                    return result;
                });

        String symbol = "BNB";
        Transfer transfer = new Transfer();
        transfer.setCoin(symbol);
        System.out.println(wallet.getAddress());
        transfer.setFromAddress(wallet.getAddress());
        transfer.setToAddress("tbnb16hywxpvvkaz6cecjz89mf2w0da3vfeg6z6yky2");
        transfer.setAmount("0.00001");
        TransactionOption options = new TransactionOption("hello BNB😊", 1, null);
//post transaction
        List<TransactionMetadata> resp = binanceDexNodeApi.transfer(transfer, wallet, options, true);
        System.out.println(resp.get(0).getHash());
    }

    @Test
    public void compressPublicKeyTest(){
        compressPublicKey("043e5eb5d55a37d325b438c563e9220ed67676c647d79070295ea028873ff8dcac0d863c385b49e7f441310a41bbd034f10cef4aa0db326b151742283abc23a666");
    }

    public String compressPublicKey(String toCompress) {
        System.out.println(toCompress.substring(0, 2));
        System.out.println(toCompress.substring(128, 130));
        System.out.println(Integer.parseInt(toCompress.substring(128, 130), 16));
        if (Integer.parseInt(toCompress.substring(128, 130), 16) % 2 == 0)
            return  "02" + toCompress.substring(2, 66);
        return "03" + toCompress.substring(2, 66);
    }

    @Test
    public void getTx(){
        final List<Transaction> blockTransactions = binanceDexNodeApi.getBlockTransactions(1288291L);
        for (Transaction blockTransaction : blockTransactions) {
            System.out.println(blockTransaction.getHash());
        }
    }
}
