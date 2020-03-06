package com.binance.dex.api.client.impl;

import com.binance.dex.api.client.*;
import com.binance.dex.api.client.domain.*;
import com.binance.dex.api.client.domain.broadcast.*;
import com.binance.dex.api.client.domain.request.ClosedOrdersRequest;
import com.binance.dex.api.client.domain.request.OpenOrdersRequest;
import com.binance.dex.api.client.domain.request.TradesRequest;
import com.binance.dex.api.client.domain.request.TransactionsRequest;
import com.binance.dex.api.client.encoding.message.TransactionRequestAssembler;
import okhttp3.RequestBody;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * Binance dex API rest client, supporting synchronous/blocking access Binance dex's REST API.
 */
public class BinanceDexApiRestClientImpl implements BinanceDexApiRestClient {
    private BinanceDexApi binanceDexApi;
    private static final okhttp3.MediaType MEDIA_TYPE = okhttp3.MediaType.parse("text/plain; charset=utf-8");

    public BinanceDexApiRestClientImpl(String baseUrl) {
        this.binanceDexApi = BinanceDexApiClientGenerator.createService(BinanceDexApi.class, baseUrl);
    }

    public BinanceDexApiRestClientImpl(String baseUrl,String apiKey){
        if(StringUtils.isBlank(apiKey)){
            this.binanceDexApi = BinanceDexApiClientGenerator.createService(BinanceDexApi.class, baseUrl);
        }else{
            this.binanceDexApi = BinanceDexApiClientGenerator.createService(BinanceDexApi.class,apiKey,baseUrl + "/internal/");
        }
    }

    public Time getTime() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getTime());
    }

    public Infos getNodeInfo() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getNodeInfo());
    }

    public Validators getValidators() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getValidators());
    }

    public List<Peer> getPeers() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getPeers());
    }

    public List<Fees> getFees() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getFees());
    }

    public List<Market> getMarkets(Integer limit) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getMarkets(limit));
    }

    public Account getAccount(String address) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getAccount(address));
    }

    public AccountSequence getAccountSequence(String address) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getAccountSequence(address));
    }

    public TransactionMetadata getTransactionMetadata(String hash) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getTransactionMetadata(hash));
    }

    public List<Token> getTokens(Integer limit) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getTokens(limit));
    }

    public OrderBook getOrderBook(String symbol, Integer limit) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getOrderBook(symbol, limit));
    }

    public List<Candlestick> getCandleStickBars(String symbol, CandlestickInterval interval) {
        return getCandleStickBars(symbol, interval, null, null, null);
    }

    public List<Candlestick> getCandleStickBars(String symbol, CandlestickInterval interval, Integer limit, Long startTime, Long endTime) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getCandlestickBars(symbol, interval.getIntervalId(), limit, startTime, endTime));
    }

    public OrderList getOpenOrders(String address) {
        OpenOrdersRequest request = new OpenOrdersRequest();
        request.setAddress(address);
        return getOpenOrders(request);
    }

    public OrderList getOpenOrders(OpenOrdersRequest request) {
        return BinanceDexApiClientGenerator.executeSync(
                binanceDexApi.getOpenOrders(request.getAddress(), request.getLimit(),
                        request.getOffset(), request.getSymbol(), request.getTotal()));
    }

    public OrderList getClosedOrders(String address) {
        ClosedOrdersRequest request = new ClosedOrdersRequest();
        request.setAddress(address);
        return getClosedOrders(request);
    }

    public OrderList getClosedOrders(ClosedOrdersRequest request) {
        Integer side = request.getSide() == null ? null : (int)(request.getSide().toValue());
        List<String> statusStrList = null;
        if (request.getStatus() != null)
            statusStrList = request.getStatus().stream().map(s -> s.name()).collect(Collectors.toList());
        return BinanceDexApiClientGenerator.executeSync(
                binanceDexApi.getClosedOrders(request.getAddress(), request.getEnd(), request.getLimit(),
                        request.getLimit(), side, request.getStart(), statusStrList, request.getSymbol(),
                        request.getTotal()));
    }

    public Order getOrder(String id) {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.getOrder(id));
    }

    public List<TickerStatistics> get24HrPriceStatistics() {
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.get24HrPriceStatistics());
    }

    public List<TickerStatistics> get24HrPriceStatistics(String symbol){
        return BinanceDexApiClientGenerator.executeSync(binanceDexApi.get24HrPriceStatistics(symbol));
    }

    @Override
    public TradePage getTrades() {
        TradesRequest request = new TradesRequest();
        return getTrades(request);
    }

    @Override
    public TradePage getTrades(TradesRequest request) {
        Integer side = request.getSide() == null ? null : (int)(request.getSide().toValue());
        return BinanceDexApiClientGenerator.executeSync(
                binanceDexApi.getTrades(
                        request.getAddress(), request.getBuyerOrderId(),
                        request.getEnd(), request.getHeight(), request.getLimit(), request.getOffset(),
                        request.getQuoteAsset(), request.getSellerOrderId(), side,
                        request.getStart(), request.getSymbol(), request.getTotal()));
    }

    @Override
    public TransactionPage getTransactions(String address) {
        TransactionsRequest request = new TransactionsRequest();
        request.setAddress(address);
        return getTransactions(request);
    }

    @Override
    public TransactionPage getTransactions(TransactionsRequest request) {
        String sideStr = request.getSide() != null ? request.getSide().name() : null;
        String txTypeStr = request.getTxType() != null ? request.getTxType().name() : null;
        return BinanceDexApiClientGenerator.executeSync(
                binanceDexApi.getTransactions(
                        request.getAddress(), request.getBlockHeight(), request.getEndTime(),
                        request.getLimit(), request.getOffset(), sideStr,
                        request.getStartTime(), request.getTxAsset(), txTypeStr));
    }

    // Broadcast and handle account sequence
    private List<TransactionMetadata> broadcast(RequestBody requestBody, boolean sync, Wallet wallet) {
        try {
            List<TransactionMetadata> metadatas =
                    BinanceDexApiClientGenerator.executeSync(binanceDexApi.broadcast(sync, requestBody));
            if (!metadatas.isEmpty() && metadatas.get(0).isOk()) {
                wallet.increaseAccountSequence();
            }
            return metadatas;
        } catch (BinanceDexApiException e) {
            wallet.invalidAccountSequence();
            throw e;
        }
    }

    public List<TransactionMetadata> newOrder(NewOrder newOrder, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildNewOrder(newOrder);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> vote(Vote vote, Wallet wallet, TransactionOption options, boolean sync) throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildVote(vote);
        return broadcast(requestBody,sync,wallet);
    }

    public List<TransactionMetadata> cancelOrder(CancelOrder cancelOrder, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildCancelOrder(cancelOrder);
        return broadcast(requestBody, sync, wallet);
    }

    public List<TransactionMetadata> transfer(Transfer transfer, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildTransfer(transfer);
        return broadcast(requestBody, sync, wallet);
    }

    public List<TransactionMetadata> multiTransfer(MultiTransfer multiTransfer, Wallet wallet, TransactionOption options,
                                            boolean sync) throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildMultiTransfer(multiTransfer);
        return broadcast(requestBody, sync, wallet);

    }

    public List<TransactionMetadata> freeze(TokenFreeze freeze, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildTokenFreeze(freeze);
        return broadcast(requestBody, sync, wallet);
    }

    public List<TransactionMetadata> unfreeze(TokenUnfreeze unfreeze, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildTokenUnfreeze(unfreeze);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> htlt(HtltReq htltReq, Wallet wallet, TransactionOption options, boolean sync)
            throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildHtlt(htltReq);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> depositHtlt(String swapId, List<com.binance.dex.api.client.encoding.message.Token> amount, Wallet wallet, TransactionOption options, boolean sync) throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildDepositHtlt(swapId,amount);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> claimHtlt(String swapId, byte[] randomNumber, Wallet wallet, TransactionOption options, boolean sync) throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildClaimHtlt(swapId,randomNumber);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> refundHtlt(String swapId, Wallet wallet, TransactionOption options, boolean sync) throws IOException, NoSuchAlgorithmException {
        wallet.ensureWalletIsReady(this);
        TransactionRequestAssembler assembler = new TransactionRequestAssembler(wallet, options);
        RequestBody requestBody = assembler.buildRefundHtlt(swapId);
        return broadcast(requestBody, sync, wallet);
    }

    @Override
    public List<TransactionMetadata> broadcast(String payload, boolean sync) {
        try {
            return BinanceDexApiClientGenerator.executeSync(binanceDexApi.broadcast(sync, RequestBody.create(MEDIA_TYPE, payload)));
        } catch (BinanceDexApiException e) {
            throw e;
        }
    }
}
