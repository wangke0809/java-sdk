package com.binance.dex.api.client;

import com.binance.dex.api.client.domain.Account;
import com.binance.dex.api.client.domain.AccountSequence;
import com.binance.dex.api.client.domain.Infos;
import com.binance.dex.api.client.encoding.Crypto;
import com.binance.dex.api.client.encoding.message.MessageType;
import com.binance.dex.api.client.ledger.LedgerDevice;
import com.binance.dex.api.client.ledger.LedgerKey;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.bitcoinj.core.ECKey;
import org.spongycastle.util.encoders.Hex;

import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Wallet {
    private final static Map<BinanceDexEnvironment, String> CHAIN_IDS = new HashMap<>();
    private Long previousSequence = null;
    private Boolean usePreviousSequence;
    private String privateKey;
    private LedgerKey ledgerKey;
    private String address;
    private ECKey ecKey;
    private byte[] addressBytes;
    private byte[] pubKeyForSign;
    private Integer accountNumber;
    private Long sequence = null;
    private BinanceDexEnvironment env;

    private String chainId;

    /**
     * add by wangke start
     **/
    private SignCallBack signCallBack;

    public SignCallBack getSignCallBack() {
        return signCallBack;
    }

    /**
     * 签名回调
     *
     * @param publicKey
     * @param env
     * @param signCallBack
     */
    public Wallet(String publicKey, BinanceDexEnvironment env, SignCallBack signCallBack) {
        if (!StringUtils.isEmpty(publicKey)) {
            // BNB 生成地址用的是压缩后公钥，这里判断和转换一下
            if ("04".equals(publicKey.substring(0, 2)) && publicKey.length() == 130) {
                if (Integer.parseInt(publicKey.substring(128, 130), 16) % 2 == 0) {
                    publicKey = "02" + publicKey.substring(2, 66);
                } else {
                    publicKey = "03" + publicKey.substring(2, 66);
                }

            }
            this.signCallBack = signCallBack;
            this.privateKey = null;
            this.env = env;
            this.ecKey = ECKey.fromPublicOnly(Hex.decode(publicKey));
            this.address = Crypto.getAddressFromECKey(this.ecKey, env.getHrp());
            this.addressBytes = Crypto.decodeAddress(this.address);
            byte[] pubKey = ecKey.getPubKeyPoint().getEncoded(true);
            byte[] pubKeyPrefix = MessageType.PubKey.getTypePrefixBytes();
            this.pubKeyForSign = new byte[pubKey.length + pubKeyPrefix.length + 1];
            System.arraycopy(pubKeyPrefix, 0, this.pubKeyForSign, 0, pubKeyPrefix.length);
            pubKeyForSign[pubKeyPrefix.length] = (byte) 33;
            System.arraycopy(pubKey, 0, this.pubKeyForSign, pubKeyPrefix.length + 1, pubKey.length);
        } else {
            throw new IllegalArgumentException("Private key cannot be empty.");
        }
    }

    /**
     * add by wangke end
     **/

    public Wallet(String privateKey, BinanceDexEnvironment env, Boolean usePreviousSequence){
        this(privateKey,env);
        if (usePreviousSequence == null) {
            this.usePreviousSequence = false;
        }else{
            this.usePreviousSequence = usePreviousSequence;
        }
    }

    public Wallet(String privateKey, BinanceDexEnvironment env) {
        if (!StringUtils.isEmpty(privateKey)) {
            this.privateKey = privateKey;
            this.env = env;
            this.usePreviousSequence = false;
            this.previousSequence = 0L;
            this.ecKey = ECKey.fromPrivate(new BigInteger(privateKey, 16));
            this.address = Crypto.getAddressFromECKey(this.ecKey, env.getHrp());
            this.addressBytes = Crypto.decodeAddress(this.address);
            byte[] pubKey = ecKey.getPubKeyPoint().getEncoded(true);
            byte[] pubKeyPrefix = MessageType.PubKey.getTypePrefixBytes();
            this.pubKeyForSign = new byte[pubKey.length + pubKeyPrefix.length + 1];
            System.arraycopy(pubKeyPrefix, 0, this.pubKeyForSign, 0, pubKeyPrefix.length);
            pubKeyForSign[pubKeyPrefix.length] = (byte) 33;
            System.arraycopy(pubKey, 0, this.pubKeyForSign, pubKeyPrefix.length + 1, pubKey.length);
        } else {
            throw new IllegalArgumentException("Private key cannot be empty.");
        }
    }

    public Wallet(int[]bip44Path, LedgerDevice ledgerDevice, BinanceDexEnvironment env) throws IOException {
        this.ledgerKey = new LedgerKey(ledgerDevice, bip44Path, env.getHrp());
        this.env = env;
        this.address = this.ledgerKey.getAddress();
        this.addressBytes = Crypto.decodeAddress(this.address);
        byte[] pubKey = this.ledgerKey.getPubKey();
        byte[] pubKeyPrefix = MessageType.PubKey.getTypePrefixBytes();
        this.pubKeyForSign = new byte[pubKey.length + pubKeyPrefix.length + 1];
        System.arraycopy(pubKeyPrefix, 0, this.pubKeyForSign, 0, pubKeyPrefix.length);
        pubKeyForSign[pubKeyPrefix.length] = (byte) 33;
        System.arraycopy(pubKey, 0, this.pubKeyForSign, pubKeyPrefix.length + 1, pubKey.length);
        this.accountNumber = 0;
        this.sequence = 0L;
    }

    public static Wallet createRandomWallet(BinanceDexEnvironment env) throws IOException {
        return createWalletFromMnemonicCode(Crypto.generateMnemonicCode(), env);
    }

    public static Wallet createWalletFromMnemonicCode(List<String> words, BinanceDexEnvironment env) throws IOException {
        String privateKey = Crypto.getPrivateKeyFromMnemonicCode(words);
        return new Wallet(privateKey, env);
    }

    public synchronized void initAccount(BinanceDexApiRestClient client) {
        Account account = client.getAccount(this.address);
        if (account != null) {
            this.accountNumber = account.getAccountNumber();
            this.sequence = account.getSequence();
        } else {
            throw new IllegalStateException(
                "Cannot get account information for address " + this.address +
                    " (does this account exist on the blockchain yet?)");
        }
    }

    public synchronized void reloadAccountSequence(BinanceDexApiRestClient client) {
        AccountSequence accountSequence = client.getAccountSequence(this.address);
        this.sequence = accountSequence.getSequence();
        if (this.usePreviousSequence && this.sequence < this.previousSequence) {
            this.sequence = this.previousSequence;
        }
        this.previousSequence = this.sequence;
    }

    public synchronized void increaseAccountSequence() {
        if (this.sequence != null){
            this.sequence++;
            this.previousSequence = this.sequence;
        }
    }

    public synchronized void decreaseAccountSequence() {
        if (this.sequence != null)
            this.sequence--;
    }

    public synchronized long getSequence() {
        if (sequence == null)
            throw new IllegalStateException("Account sequence is not initialized.");
        return sequence;
    }

    public synchronized void setAccountNumber(Integer accountNumber) {
        this.accountNumber = accountNumber;
    }

    public synchronized void setSequence(Long sequence) {
        this.sequence = sequence;
    }

    public synchronized void setChainId(String chainId) {
        this.chainId = chainId;
    }

    public synchronized void invalidAccountSequence() {
        this.previousSequence = sequence;
        this.sequence = null;
    }

    public synchronized void ensureWalletIsReady(BinanceDexApiRestClient client) {
        if (accountNumber == null) {
            initAccount(client);
        }
        if (sequence == null) {
            reloadAccountSequence(client);
        }

        if (chainId == null) {
            chainId = CHAIN_IDS.get(chainId);
            if (chainId == null) {
                initChainId(client);
            }
        }
    }

    public synchronized void initChainId(BinanceDexApiRestClient client) {
        Infos info = client.getNodeInfo();
        chainId = info.getNodeInfo().getNetwork();
        CHAIN_IDS.put(env, chainId);
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public String getAddress() {
        return address;
    }

    public ECKey getEcKey() {
        return ecKey;
    }

    public LedgerKey getLedgerKey() {
        return ledgerKey;
    }

    public byte[] getPubKeyForSign() {
        return pubKeyForSign;
    }

    public int getAccountNumber() {
        return accountNumber;
    }

    public String getChainId() {
        return chainId;
    }

    public byte[] getAddressBytes() {
        return addressBytes;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, BinanceDexConstants.BINANCE_DEX_TO_STRING_STYLE)
                .append("addressBytes", addressBytes)
                .append("address", address)
                .append("ecKey", ecKey)
                .append("pubKeyForSign", pubKeyForSign)
                .append("accountNumber", accountNumber)
                .append("sequence", sequence)
                .append("chainId", chainId)
                .toString();
    }
}
