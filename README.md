# `FlexfillsApi`

The `FlexfillsApi` is a package for using Flex Fills WebSocket communication with FlexFills trading services.

## Installation

## Usage

```java
import com.flexfills.app.FlexfillsApi;

public class Main {
  public static void main(String[] args) throws Exception {
    FlexfillsApi flexfillsApi = new FlexfillsApi("username", "password", true);

    flexfillsApi.initialize();

    Map<String, Object> resp = flexfillsApi.getAssetList();

    System.out.println(resp);
  }
}
```

### Available Functions

<table class="table table-bordered">
    <thead class="thead-light">
        <tr>
            <th>Functions</th>
            <th>Params</th>
            <th>Explaination</th>
        </tr>
    </thead>
    <tbody>
        <tr>
            <td><code class="highlighter-rouge">getAssetList()</code></td>
            <td></td>
            <td>Provides a list of supported assets and their trading specifications.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getInstrumentList()</code></td>
            <td></td>
            <td>Provides a list of supported Instruments and their trading specifications.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">subscribeOrderBooks(List<String> instruments, Callable<Map<String, Object>> callback)</code></td>
            <td><p><strong>instruments:</strong> list of pair of currencies. All available pairs are:</p>
            <code class="highlighter-rouge">BTC/PLN, DASH/PLN, EUR/GBP, LTC/GBP, LTC/USD, OMG/EUR, OMG/PLN, USDT/EUR, XRP/USD, ZEC/BTC, ZEC/PLN, ZRX/BTC, DOT/BTC, ZRX/USD, BSV/USDT, ADA/USDT, ZIL/USDT, ENJ/USD, XEM/USDT, BNB/USDT, BSV/EUR, BTC/EUR, DASH/EUR, LINK/USD, LTC/ETH, ZEC/USD, BAT/USDT, DOT/USDT, DOT/ETH, MATIC/USTD, AVAX/USDT, BAT/EUR, BAT/GBP, BCH/BTC, BTC/USDT, ETH/GBP, EUR/USD, LINK/BTC, LINK/ETH, LTC/EUR, LTC/USDT, USDT/GBP, XEM/USD, XLM/ETH, XRP/ETH, DASH/USDT, DASH/ETH, XTZ/USD, DAI/USD, ADA/USD, DOT/EUR, BAT/USD, BCH/USDC, BSV/USD, BTC/GBP, DASH/BTC, LTC/PLN, USDT/USD, XLM/BTC, XRP/PLN, ZRX/PLN, QTUM/USDT, ADA/USDC, USDT/USDC, QTUM/USD, MKR/USD, SOL/USD, ATOM/ETH, ATOM/USDT, QASH/USD, VRA/USD, BCH/ETH, BSV/PLN, BTC/USD, ETH/BTC, LTC/BTC, OMG/USD, USDC/EUR, USDC/USD, USDC/USDT, XEM/BTC, XLM/EUR, XLM/USD, XRP/EUR, BSV/ETH, XLM/USDT, ZEC/USDT, BAT/USDC, LINK/USDC, SOL/BTC, DOGE/USD, DOGE/BTC, BAT/BTC, BAT/PLN, BCH/GBP, BCH/PLN, BCH/USD, BTC/USDC, ETH/USDC, OMG/BTC, BTC-PERPETUAL, ETH-PERPETUAL, ZRX/EUR, ADA/BTC, QTUM/ETH, DOT/USD, SOL/ETH, ATOM/BTC, ETH/USDT, EUR/PLN, LINK/PLN, LINK/USDT, OMG/ETH, XRP/BTC, XRP/USDT, ZEC/EUR, ADA/EUR, ADA/PLN, DOT/PLN, OMG/USDT, EUR/USDT, DOGE/USDT, GALA/USDT, BAT/ETH, BCH/EUR, BCH/USDT, BSV/BTC, DASH/USD, ETH/EUR, ETH/PLN, ETH/USD, GBP/USD, USD/PLN, XLM/PLN, XRP/GBP, ZIL/USD, USDT/PLN, XRP/USDC, QTUM/BTC, ADA/ETH, ZIL/BTC, SOL/USDT, LUNA/USDT, ATOM/USD</code>
            <p><strong>callback:</strong> Callback function for getting streaming data.</p>
            </td>
            <td>Provides streaming services an order book for selected symbol, user needs to provide levels of order book to receive. MAX is 20. MIN is 1. Order books are sorted based on NBBO price: BIDs best (Max) first then descending, ASKs best (MIN) first then ascending. The whole Order books is updated every 20MS regardless of changes.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">unsubscribeOrderBooks(List<String> instruments)</code></td>
            <td><strong>instruments:</strong> list of pair of currencies.</td>
            <td></td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">tradeBookPublic(List<String> instruments, Callable<Map<String, Object>> callback)</code></td>
            <td>
                <p><strong>instruments:</strong> list of pair of currencies.</p>
                <p><strong>callback:</strong> Callback function for getting streaming data.</p>
            </td>
            <td>Provides streaming services a trading book (public trades) for selected symbol. Once subscribed updates will be pushed to user as they appear at FlexFills.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getBalance(List<String> currencies)</code></td>
            <td><strong>currencies:</strong> list of selected currencies.</td>
            <td></td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getPrivateTrades(List<String> instruments,
            Callable<Map<String, Object>> callback)</code></td>
            <td>
                <p><strong>instruments:</strong> list of pair of currencies.</p>
                <p><strong>callback:</strong> Callback function for getting streaming data.</p>
            </td>
            <td>Private trades subscription will provide a snapshot of currently open ACTIVE orders and then updates via WebSocket.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getOpenOrdersList(List<String> instruments)</code></td>
            <td>
                <p><strong>instruments:</strong> list of pair of currencies. optional</p>
            </td>
            <td>Get current list of open orders. One time request/response.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">createOrder(Map<String, Object> orderData)</code></td>
            <td>
                <p><strong>orderData:</strong> The dict of order data, including globalInstrumentCd, clientOrderId, orderType, timeInForce, price, amount, exchangeName, orderSubType, tradeSide
                <ul>
                    <li>globalInstrumentCd - pair of currencies (BTC/USD, ...). string</li>
                    <li>clientOrderId: Id of the order. string</li>
                    <li>orderType: <strong>market</strong> - Market order, <strong>limit</strong> - Limit order. string</li>
                    <li>timeInForce: string, optional, 
                        <ul>
                            <li><strong>GTC</strong> - Good till cancelled (default, orders are in order book for 90 days)</li>
                            <li><strong>GTD</strong> - Good till day, will terminate at end of day 4:59PM NY TIME</li>
                            <li><strong>GTT</strong> - Good till time, alive until specific date (cannot exceed 90 days)</li>
                            <li><strong>FOK</strong> - Fill or Kill, Fill full amount or nothing immediately</li>
                            <li><strong>IOC</strong> - Immediate or Cancel, Fill any amount and cancel the rest immediately</li>
                        </ul>
                    </li>
                    <li>price: optional, Price only required for limit orders</li>
                    <li>amount: Quantity of the order</li>
                    <li>exchangeName: Name of exchange to send order to, string, optional. required for direct orders</li>
                    <li>orderSubType: optional, string, POST_ONLY, only required if client wishes to submit a passive order which does not immediately fill the order, in case of immediate fill, order will be rejected</li>
                    <li>tradeSide: optional, string, Side of the order Enum buy or sell</li>
                </ul>
                </p>
            </td>
            <td>Get current list of open orders. One time request/response.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">cancelOrder(Map<String, Object> orderData)</code></td>
            <td>
                <p><strong>orderData:</strong> The dict of order data, including globalInstrumentCd, clientOrderId or exchangeOrderId
                <ul>
                    <li>globalInstrumentCd - pair of currencies (BTC/USD, ...). string</li>
                    <li>clientOrderId: Id of the order. string</li>
                    <li>exchangeOrderId: exchangeOrderId. string</li>
                </ul>
                </p>
            </td>
            <td>User may cancel existing orders; client may cancel one order by either including orderId or exchangeOrderId if orderId is not known. Only one parameter is needed and will be accepted. If no orderId or transactionId are added in the message than, all orders for selected pair/s will be cancelled. Must be subscribed to valid pair in order to cancel order in proper pair!</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">modifyOrder(Map<String, Object> orderData)</code></td>
            <td>
                <p><strong>orderData:</strong> The dict of order data, including globalInstrumentCd, clientOrderId or exchangeOrderId
                <ul>
                    <li>globalInstrumentCd - pair of currencies (BTC/USD, ...). string</li>
                    <li>clientOrderId: Id of the account. string</li>
                    <li>exchangeOrderId: clientOrdId. string</li>
                    <li>price: If price is not passed in, then it’s not modified, string</li>
                    <li>amount: If price is not passed in, then it’s not modified, string</li>
                </ul>
                </p>
            </td>
            <td>Clients may update existing orders. Amount or Price can be modified. Client must use clientOrderId or exchangeOrderId Only one parameter is needed and will be accepted</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getTradeHistory(String dateFrom, String dateTo, List<String> instruments)</code></td>
            <td>
                <p><strong>dateFrom:</strong> Start date of required time frame, string. example: "2022-12-01T00:00:00"</p>
                <p><strong>dateTo:</strong> End date of required time frame, string. example: "2022-12-31T00:00:00"</p>
                <p><strong>instruments:</strong> list of pair of currencies.</p>
            </td>
            <td>Clients may request a list <strong>PARTIALLY_FILLED</strong>, <strong>FILLED</strong> trades for a required time frame. Channel arguments ‘date-from’, ‘date-to’ are optional. If ‘date-from’ is not provided, it will be defaulted to ‘now minus 24 hours’. If ‘date-to’ is not provided, it will be defaulted to ‘now’.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getOrderHistory(String dateFrom, String dateTo, List<String> instruments,
            List<String> statues)</code></td>
            <td>
                <p><strong>dateFrom:</strong> Start date of required time frame, string. example: "2022-12-01T00:00:00"</p>
                <p><strong>dateTo:</strong> End date of required time frame, string. example: "2022-12-31T00:00:00"</p>
                <p><strong>instruments:</strong> list of pair of currencies.</p>
                <p><strong>statues:</strong> list of status.</p>
            </td>
            <td>Clients may request a list of <strong>COMLETED</strong>, <strong>REJECTED</strong>, <strong>PARTIALLY_FILLED</strong>, <strong>FILLED</strong>, <strong>EXPIRED</strong> order requests for a required time frame. Channel arguments ‘date-from’, ‘date-to’, ‘status’ are optional. If ‘date-from’ is not provided, it will be defaulted to ‘now minus 24 hours’. If ‘date-to’ is not provided, it will be defaulted to ‘now’. If ‘status‘ is not provided then trades with any status will be selected.</td>
        </tr>
        <tr>
            <td><code class="highlighter-rouge">getTradePositions()</code></td>
            <td></td>
            <td></td>
        </tr>
    </tbody>
</table>

## Contributing

Pull requests are welcome. For major changes, please open an issue first to discuss what you would like to change.

## License

[Apache 2](http://www.apache.org/licenses/LICENSE-2.0)
