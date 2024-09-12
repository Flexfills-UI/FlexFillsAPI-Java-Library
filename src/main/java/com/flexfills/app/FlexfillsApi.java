package com.flexfills.app;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpHeaders;
import java.net.http.HttpRequest.BodyPublishers;
import java.net.http.WebSocket;
import java.net.http.WebSocket.Listener;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;

/**
 * FlexfillsApi
 *
 */
public class FlexfillsApi {
    // Define Auth Urls

    private static final String BASE_DOMAIN_TEST = "test.flexfills.com";
    private static final String BASE_DOMAIN_PROD = "flexfills.com";

    // Define public and private channels

    private static final String CH_ASSET_LIST = "ASSET_LIST";
    private static final String CH_INSTRUMENT_LIST = "INSTRUMENT_LIST";
    private static final String CH_ORDER_BOOK_PUBLIC = "ORDER_BOOK_PUBLIC";
    private static final String CH_TRADE_PUBLIC = "TRADE_PUBLIC";
    private static final String CH_ACTIVE_SUBSCRIPTIONS = "ACTIVE_SUBSCRIPTIONS";

    private static final String CH_PRV_BALANCE = "BALANCE";
    private static final String CH_PRV_TRADE_PRIVATE = "TRADE_PRIVATE";
    private static final String CH_PRV_TRADE_POSITIONS = "TRADE_POSITIONS";

    // Define available constants

    private static final List<String> ORDER_DIRECTIONS = List.of("SELL", "BUY");
    private static final List<String> ORDER_TYPES = List.of("MARKET", "LIMIT", "POST_ONLY");
    private static final List<String> TIME_IN_FORCES = List.of("GTC", "GTD", "GTT", "FOK", "IOC");

    // Define socket urls

    private static final String WS_URL_TEST = "wss://test.flexfills.com/exchange/ws";
    private static final String WS_URL_PROD = "wss://flexfills.com/exchange/ws";

    String _username;
    String _password;
    boolean _isTest;
    String _authToken;
    String _socketUrl;

    public FlexfillsApi(String username, String password, boolean isTest) {
        _username = username;
        _password = password;
        _isTest = isTest || false;
        _socketUrl = _isTest ? WS_URL_TEST : WS_URL_PROD;
    }

    public interface Callable<T> {
        void call(Map<String, Object> response);
    }

    public String initialize() throws InterruptedException, ExecutionException {

        String connUrl = _isTest ? BASE_DOMAIN_TEST : BASE_DOMAIN_PROD;

        String payload = "username=" + _username + "&password=" + _password;

        HttpClient client = HttpClient.newBuilder().build();

        // First request to get JSESSIONID
        HttpRequest sessionRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://" + connUrl + "/auth/login"))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .POST(BodyPublishers.ofString(payload))
                .build();

        CompletableFuture<HttpResponse<String>> future1 = client.sendAsync(sessionRequest,
                HttpResponse.BodyHandlers.ofString());

        String jsessionId = future1.thenApplyAsync((HttpResponse<String> response1) -> {
            // Extract JSESSIONID from the first response
            HttpHeaders headers = response1.headers();
            String setCookieHeader = headers.firstValue("Set-Cookie").orElse("");
            String jsessionIdTemp = "";
            for (String cookie : setCookieHeader.split(";")) {
                if (cookie.trim().startsWith("JSESSIONID")) {
                    jsessionIdTemp = cookie.split("=")[1];
                    break;
                }
            }
            if (jsessionIdTemp.isEmpty()) {
                throw new RuntimeException("Could not authenticate.");
            }
            return jsessionIdTemp;
        }).get(); // Wait for the first request to complete and get the JSESSIONID

        // Second request to get auth token
        HttpRequest tokenRequest = HttpRequest.newBuilder()
                .uri(URI.create("https://" + connUrl + "/auth/auth/jwt/clients/" + _username + "/token"))
                .header("Accept", "*/*")
                .header("Cookie", "JSESSIONID=" + jsessionId)
                .header("clientSecret", _password)
                .POST(BodyPublishers.noBody())
                .build();

        CompletableFuture<HttpResponse<String>> future2 = client.sendAsync(tokenRequest,
                HttpResponse.BodyHandlers.ofString());

        _authToken = future2.thenApplyAsync((HttpResponse<String> response2) -> {
            if (response2.statusCode() != 200 || response2.body().isEmpty()) {
                throw new RuntimeException("Could not authenticate.");
            }
            return response2.body();
        }).get(); // Wait for the second request to complete and get the auth token

        return _authToken;
    }

    public Map<String, Object> getAssetList() throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("channel", CH_ASSET_LIST);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> getInstrumentList() throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("channel", CH_INSTRUMENT_LIST);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> subscribeOrderBooks(List<String> instruments, Callable<Map<String, Object>> callback)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "SUBSCRIBE");
        message.put("channel", CH_ORDER_BOOK_PUBLIC);

        List<Map<String, String>> channelArgs = List.of(
                Map.of("name", "instrument", "value", "[" + String.join(", ", instruments) + "]"));
        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, callback, false).get();

        return resp;
    }

    public Map<String, Object> unsubscribeOrderBooks(List<String> instruments)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "UNSUBSCRIBE");
        message.put("channel", CH_ORDER_BOOK_PUBLIC);

        List<Map<String, String>> channelArgs = List.of(
                Map.of("name", "instrument", "value", "[" + String.join(", ", instruments) + "]"));
        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> tradeBookPublic(List<String> instruments, Callable<Map<String, Object>> callback)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "SUBSCRIBE");
        message.put("channel", CH_TRADE_PUBLIC);

        List<Map<String, String>> channelArgs = new ArrayList<>();
        Map<String, String> instrumentArg = new HashMap<>();
        instrumentArg.put("name", "instrument");
        instrumentArg.put("value", "[" + String.join(", ", instruments) + "]");
        channelArgs.add(instrumentArg);

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, callback, false).get();

        return resp;
    }

    public Map<String, Object> getBalance(List<String> currencies) throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "SUBSCRIBE");
        message.put("channel", CH_PRV_BALANCE);
        message.put("signature", _authToken);

        List<Map<String, String>> channelArgs = new ArrayList<>();
        Map<String, String> instrumentArg = new HashMap<>();
        instrumentArg.put("name", "currency");
        instrumentArg.put("value", "[" + String.join(", ", currencies) + "]");
        channelArgs.add(instrumentArg);

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> getPrivateTrades(List<String> instruments,
            Callable<Map<String, Object>> callback) throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "SUBSCRIBE");
        message.put("channel", CH_PRV_TRADE_PRIVATE);
        message.put("signature", _authToken);

        List<Map<String, String>> channelArgs = new ArrayList<>();
        Map<String, String> instrumentArg = new HashMap<>();
        instrumentArg.put("name", "instrument");
        instrumentArg.put("value", "[" + String.join(", ", instruments) + "]");
        channelArgs.add(instrumentArg);

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, callback, false).get();

        return resp;
    }

    public Map<String, Object> getOpenOrdersList(List<String> instruments)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);

        List<Map<String, String>> channelArgs = new ArrayList<>();
        Map<String, String> categoryArg = new HashMap<>();
        categoryArg.put("name", "category");
        categoryArg.put("value", "ACTIVE_ORDERS");
        channelArgs.add(categoryArg);

        if (instruments != null && !instruments.isEmpty()) {
            Map<String, String> instrumentArg = new HashMap<>();
            instrumentArg.put("name", "instrument");
            instrumentArg.put("value", "[" + String.join(", ", instruments) + "]");
            channelArgs.add(instrumentArg);
        }

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> createOrder(Map<String, Object> orderData)
            throws Exception {
        List<String> requiredKeys = List.of("globalInstrumentCd", "direction",
                "orderType", "amount");

        List<String> optionalKeys = List.of("exchangeName", "orderSubType", "price", "clientOrderId",
                "timeInForce", "tradeSide");

        validatePayload(orderData, requiredKeys, List.of(), "orderData");

        if (orderData.get("orderType").toString().toUpperCase() == "LIMIT" && !orderData.containsKey("price")) {
            throw new Exception("Price should be included in orderData.");
        }

        // if (orderData.get("requestType").toString().toUpperCase() == "DIRECT"
        // && !orderData.containsKey("exchangeName")) {
        // throw new Exception("Direct orders need to have exchangeName.");
        // }

        // Before sending the new order, request user must first be subscribed to
        // desired pair, otherwise order will be rejected.

        Map<String, Object> subscribeMessage = new HashMap<>();
        subscribeMessage.put("command", "SUBSCRIBE");
        subscribeMessage.put("signature", _authToken);
        subscribeMessage.put("channel", CH_PRV_TRADE_PRIVATE);

        List<Map<String, String>> channelArgs = List.of(
                Map.of("name", "instrument", "value", "[" + orderData.get("globalInstrumentCd").toString() + "]"));
        subscribeMessage.put("channelArgs", channelArgs);

        Map<String, Object> orderPayload = new HashMap<>();
        orderPayload.put("class", "Order");
        orderPayload.put("globalInstrumentCd", orderData.get("globalInstrumentCd").toString());
        orderPayload.put("direction", orderData.get("direction").toString().toUpperCase());
        orderPayload.put("orderType", orderData.get("orderType").toString().toUpperCase());
        orderPayload.put("amount", orderData.get("amount").toString());

        for (String okey : optionalKeys) {
            if (orderData.containsKey(okey)) {
                orderPayload.put(okey, String.valueOf(orderData.get(okey)));
            }
        }

        Map<String, Object> message = new HashMap<>();
        message.put("command", "CREATE");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);
        message.put("data", List.of(orderPayload));

        Map<String, Object> resp = subscribeAndSendMessage(subscribeMessage, message, null).get();

        return resp;
    }

    public Map<String, Object> cancelOrder(Map<String, Object> orderData) throws Exception {

        List<String> requiredKeys = List.of("globalInstrumentCd", "clientOrderId", "direction",
                "orderType", "timeInForce", "price", "amount", "exchange");

        Map<String, Object> validData = validatePayload(orderData, requiredKeys, List.of(), "orderData");

        validData.put("class", "Order");

        Map<String, Object> subscribeMessage = new HashMap<>();
        subscribeMessage.put("command", "SUBSCRIBE");
        subscribeMessage.put("signature", _authToken);
        subscribeMessage.put("channel", CH_PRV_TRADE_PRIVATE);

        List<Map<String, String>> channelArgs = List.of(
                Map.of("name", "instrument", "value", "[" + orderData.get("globalInstrumentCd").toString() + "]"));
        subscribeMessage.put("channelArgs", channelArgs);

        Map<String, Object> message = new HashMap<>();
        message.put("command", "CANCEL");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);
        message.put("data", new Map[] { validData });

        Map<String, Object> resp = subscribeAndSendMessage(subscribeMessage, message, null).get();

        return resp;
    }

    public Map<String, Object> modifyOrder(Map<String, Object> orderData)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "MODIFY");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);

        Map<String, Object> orderDetails = new HashMap<>();
        orderDetails.put("class", "Order");
        orderDetails.put("globalInstrumentCd", String.valueOf(orderData.get("globalInstrumentCd")));
        orderDetails.put("orderId", String.valueOf(orderData.get("orderId")));
        orderDetails.put("amount", String.valueOf(orderData.get("amount")));

        if (orderData.containsKey("price")) {
            orderDetails.put("price", String.valueOf(orderData.get("price")));
        }

        if (orderData.containsKey("amount")) {
            orderDetails.put("amount", String.valueOf(orderData.get("amount")));
        }

        message.put("data", new Map[] { orderDetails });

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> getTradeHistory(String dateFrom, String dateTo, List<String> instruments)
            throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);

        List<Map<String, String>> channelArgs = new ArrayList<>();
        channelArgs.add(createChannelArg("category", "TRADES_HISTORY"));
        channelArgs.add(createChannelArg("instrument", "[" + String.join(", ", instruments) + "]"));
        channelArgs.add(createChannelArg("date-from", dateFrom));
        channelArgs.add(createChannelArg("date-to", dateTo));

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> getOrderHistory(String dateFrom, String dateTo, List<String> instruments,
            List<String> statues) throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("signature", _authToken);
        message.put("channel", CH_PRV_TRADE_PRIVATE);

        List<Map<String, String>> channelArgs = List.of(
                Map.of("name", "category", "value", "ORDERS_HISTORY"),
                Map.of("name", "instrument", "value", "[" + String.join(", ", instruments) + "]"),
                Map.of("name", "date-from", "value", dateFrom),
                Map.of("name", "date-to", "value", dateTo),
                Map.of("name", "status", "value", "[" + String.join(", ", statues) + "]"));

        message.put("channelArgs", channelArgs);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    public Map<String, Object> getTradePositions() throws InterruptedException, ExecutionException {
        Map<String, Object> message = new HashMap<>();
        message.put("command", "GET");
        message.put("channel", CH_PRV_TRADE_POSITIONS);

        Map<String, Object> resp = sendMessage(message, null, false).get();

        return resp;
    }

    // --------------------
    // Protected Methods
    // --------------------

    private Map<String, String> createChannelArg(String name, String value) {
        Map<String, String> arg = new HashMap<>();
        arg.put("name", name);
        arg.put("value", value);
        return arg;
    }

    protected CompletableFuture<Map<String, Object>> sendMessage(Map<String, Object> message,
            Callable<Map<String, Object>> callback, boolean isOneTime) {

        CompletableFuture<Map<String, Object>> futureResponse = new CompletableFuture<>();
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        client.newWebSocketBuilder()
                .header("Authorization", _authToken)
                .buildAsync(URI.create(_socketUrl), new Listener() {
                    private int count = 0;
                    private Map<String, Object> jsonResponseMap = new HashMap<>();
                    private StringBuilder messageBuffer = new StringBuilder();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        try {
                            String jsonMessage = objectMapper.writeValueAsString(message);
                            webSocket.sendText(jsonMessage, true);
                        } catch (Exception e) {
                            futureResponse.completeExceptionally(e);
                        }
                        Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        messageBuffer.append(data);

                        if (last) {
                            boolean isValid;
                            String response = messageBuffer.toString();
                            messageBuffer.setLength(0); // Clear the buffer

                            try {
                                JsonNode jsonResponse = objectMapper.readTree(response);
                                jsonResponseMap = objectMapper.convertValue(jsonResponse,
                                        new TypeReference<Map<String, Object>>() {
                                        });

                                isValid = validateResponse(jsonResponse, message);
                                // validatedResp = jsonResponse.toString();
                            } catch (Exception e) {
                                futureResponse.completeExceptionally(e);
                                return Listener.super.onText(webSocket, data, last);
                            }

                            if (callback != null) {
                                callback.call(jsonResponseMap);
                            } else {
                                if (isOneTime || isValid) {
                                    futureResponse.complete(jsonResponseMap);
                                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done");
                                } else if (count >= 10) {
                                    futureResponse.completeExceptionally(new RuntimeException("Validation failed"));
                                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done");
                                }
                                count++;
                            }
                        }

                        return Listener.super.onText(webSocket, data, last);
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        futureResponse.completeExceptionally(error);
                    }
                });

        return futureResponse;

    }

    public CompletableFuture<Map<String, Object>> subscribeAndSendMessage(Map<String, Object> subscriber,
            Map<String, Object> message,
            Callable<Map<String, Object>> callback) {
        CompletableFuture<Map<String, Object>> futureResponse = new CompletableFuture<>();
        HttpClient client = HttpClient.newHttpClient();
        ObjectMapper objectMapper = new ObjectMapper();

        client.newWebSocketBuilder()
                .header("Authorization", _authToken)
                .buildAsync(URI.create(_socketUrl), new Listener() {
                    private Map<String, Object> jsonResponseMap = new HashMap<>();

                    @Override
                    public void onOpen(WebSocket webSocket) {
                        try {
                            String jsonSubscriber = objectMapper.writeValueAsString(subscriber);
                            webSocket.sendText(jsonSubscriber, true);
                        } catch (Exception e) {
                            futureResponse.completeExceptionally(e);
                        }
                        Listener.super.onOpen(webSocket);
                    }

                    @Override
                    public CompletionStage<?> onText(WebSocket webSocket, CharSequence data, boolean last) {
                        String response = data.toString();
                        JsonNode jsonResponse;

                        try {
                            jsonResponse = objectMapper.readTree(response);
                            // isValid = validateResponse(jsonResponse, subscriber);
                            jsonResponseMap = objectMapper.convertValue(jsonResponse,
                                    new TypeReference<Map<String, Object>>() {
                                    });
                            if (jsonResponse.has("event") && jsonResponse.get("event").asText().equals("ERROR")) {
                                futureResponse.complete(jsonResponseMap);
                                webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done");
                                return Listener.super.onText(webSocket, data, last);
                            }

                            if (jsonResponse.has("command")
                                    && !jsonResponse.get("command").asText().equals("SUBSCRIBE")) {
                                if (callback != null) {
                                    callback.call(jsonResponseMap);
                                } else {
                                    futureResponse.complete(jsonResponseMap);
                                    webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done");
                                    return Listener.super.onText(webSocket, data, last);
                                }
                            }
                        } catch (JsonProcessingException e) {
                            futureResponse.completeExceptionally(e);
                            return Listener.super.onText(webSocket, data, last);
                        }

                        try {
                            String jsonMessage = objectMapper.writeValueAsString(message);
                            if (jsonResponse.has("command")
                                    && jsonResponse.get("command").asText().equals("SUBSCRIBE")) {
                                webSocket.sendText(jsonMessage, true);
                            }
                        } catch (Exception e) {
                            futureResponse.completeExceptionally(e);
                        }

                        return Listener.super.onText(webSocket, data, last);

                        // return webSocket.request(1).thenApply()
                        // .thenCompose(v -> webSocket.receive())
                        // .thenApply(WebSocket.MessagePart::data)
                        // .thenApply(CharSequence::toString)
                        // .thenApply(response2 -> {
                        // private Map<String, Object> jsonResponseMap2 = new HashMap<>();

                        // try {
                        // JsonNode jsonResponse2 = objectMapper.readTree(response2);
                        // jsonResponseMap2 = objectMapper.convertValue(jsonResponse2,
                        // new TypeReference<Map<String, Object>>() {
                        // });
                        // } catch (JsonProcessingException e) {
                        // futureResponse.completeExceptionally(e);
                        // }

                        // if (callback != null) {
                        // callback.call(jsonResponseMap2);
                        // } else {
                        // futureResponse.complete(jsonResponseMap2);
                        // webSocket.sendClose(WebSocket.NORMAL_CLOSURE, "Done");
                        // }
                        // return jsonResponseMap2;
                        // });
                    }

                    @Override
                    public void onError(WebSocket webSocket, Throwable error) {
                        futureResponse.completeExceptionally(error);
                    }
                });

        return futureResponse;
    }

    private boolean validateResponse(JsonNode response, Object message) {
        if (message == null || !response.has("command")) {
            return true;
        }

        if (response.has("event") && response.get("event").asText().equals("ERROR")) {
            return true;
        }

        if (response.has("event") && response.get("event").asText().equals("ACK")) {
            return false;
        }

        return true;
    }

    public static Map<String, Object> validatePayload(Map<String, Object> payload, List<String> requiredKeys,
            List<String> optionalKeys,
            String dataType) throws Exception {

        Map<String, Object> validData = new HashMap<>();

        if (requiredKeys != null) {
            for (String key : requiredKeys) {
                if (payload.containsKey(key)) {
                    if (payload.get(key) == null) {
                        validData.put(key, null);
                    } else {
                        validData.put(key, payload.get(key).toString());
                    }
                } else {
                    throw new Exception(key + " field should be in the "
                            + (dataType != null && !dataType.isEmpty() ? dataType : "payload data"));
                }
            }
        }

        if (optionalKeys != null) {
            for (String key : optionalKeys) {
                if (payload.containsKey(key)) {
                    validData.put(key, payload.get(key).toString());
                }
            }
        }

        if (payload.containsKey("direction")) {
            String direction = payload.get("direction").toString().toUpperCase();
            if (ORDER_DIRECTIONS.contains(direction)) {
                validData.put("direction", direction);
            } else {
                throw new Exception("The direction field is not valid in "
                        + (dataType != null && !dataType.isEmpty() ? dataType : "payload data"));
            }
        }

        if (payload.containsKey("orderType")) {
            String orderType = payload.get("orderType").toString().toUpperCase();
            if (ORDER_TYPES.contains(orderType)) {
                validData.put("orderType", orderType);
            } else {
                throw new Exception("The orderType field is not valid in "
                        + (dataType != null && !dataType.isEmpty() ? dataType : "payload data"));
            }
        }

        if (payload.containsKey("timeInForce")) {
            if (payload.get("timeInForce") == null) {
                validData.put("timeInForce", "GTC");
            } else {
                String timeInForce = payload.get("timeInForce").toString().toUpperCase();
                if (TIME_IN_FORCES.contains(timeInForce)) {
                    validData.put("timeInForce", timeInForce);
                } else {
                    throw new Exception("The timeInForce field is not valid in "
                            + (dataType != null && !dataType.isEmpty() ? dataType : "payload data"));
                }
            }
        }

        return validData;
    }
}
