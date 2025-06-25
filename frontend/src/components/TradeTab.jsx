import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Button } from "./ui/button";
import { Input } from "./ui/input";

const TradeTab = ({
  cryptos,
  selectedCrypto,
  setSelectedCrypto,
  portfolio,
  tradeType,
  setTradeType,
  tradeAmount,
  setTradeAmount,
  executeTrade,
  tradeMessage,
  isExecutingTrade,
}) => {
  // Safety check: ensure cryptos is an array
  const cryptoList = Array.isArray(cryptos) ? cryptos : [];

  if (cryptoList.length === 0) {
    return (
      <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 lg:gap-6">
        {/* Cryptocurrency Selection */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <CardTitle className="text-lg lg:text-xl">
              Select Cryptocurrency
            </CardTitle>
          </CardHeader>
          <CardContent>
            <div className="text-center text-slate-400 py-8">
              Loading cryptocurrency data...
            </div>
          </CardContent>
        </Card>

        {/* Trade Execution */}
        <Card className="bg-slate-800 border-slate-700">
          <CardHeader>
            <CardTitle className="text-lg lg:text-xl">Execute Trade</CardTitle>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="text-slate-400 text-center py-4">
              Loading trading interface...
            </div>
          </CardContent>
        </Card>
      </div>
    );
  }

  return (
    <div className="grid grid-cols-1 lg:grid-cols-2 gap-4 lg:gap-6">
      {/* Cryptocurrency Selection */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader>
          <CardTitle className="text-lg lg:text-xl">
            Select Cryptocurrency
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="max-h-96 overflow-y-auto space-y-2">
            {cryptoList.map((crypto) => (
              <div
                key={crypto.symbol}
                className={`p-3 rounded cursor-pointer transition-colors ${
                  selectedCrypto?.symbol === crypto.symbol
                    ? "bg-blue-600 text-white"
                    : "bg-slate-700/50 hover:bg-slate-700 text-slate-300"
                }`}
                onClick={() => setSelectedCrypto(crypto)}
              >
                <div className="flex justify-between items-center">
                  <div>
                    <div className="font-semibold">{crypto.name}</div>
                    <div className="text-sm opacity-75">{crypto.symbol}</div>
                  </div>
                  <div className="text-right">
                    <div className="font-mono">${crypto.price.toFixed(2)}</div>
                    <div
                      className={`text-xs ${
                        crypto.change24h >= 0
                          ? "text-green-400"
                          : "text-red-400"
                      }`}
                    >
                      {crypto.change24h >= 0 ? "+" : ""}
                      {crypto.change24h.toFixed(2)}%
                    </div>
                  </div>
                </div>
              </div>
            ))}
          </div>
        </CardContent>
      </Card>

      {/* Trade Execution */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader>
          <CardTitle className="text-lg lg:text-xl">Execute Trade</CardTitle>
        </CardHeader>
        <CardContent className="space-y-4">
          {selectedCrypto && (
            <div className="p-3 lg:p-4 bg-slate-700 rounded">
              <div className="font-semibold text-lg">
                {selectedCrypto.name} ({selectedCrypto.symbol})
              </div>
              <div className="text-2xl lg:text-3xl font-bold">
                ${selectedCrypto.price.toFixed(2)}
              </div>

              {/* Current Holdings Display */}
              <div className="mt-3 pt-3 border-t border-slate-600">
                <div className="text-sm text-slate-400 mb-1">
                  Your Holdings:
                </div>
                {portfolio[selectedCrypto.symbol] ? (
                  <div className="space-y-1">
                    <div className="flex justify-between">
                      <span className="text-sm">Quantity:</span>
                      <span className="font-mono text-blue-400">
                        {portfolio[selectedCrypto.symbol].quantity.toFixed(6)}{" "}
                        {selectedCrypto.symbol}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm">Current Value:</span>
                      <span className="font-mono text-green-400">
                        $
                        {(
                          portfolio[selectedCrypto.symbol].quantity *
                          selectedCrypto.price
                        ).toFixed(2)}
                      </span>
                    </div>
                    <div className="flex justify-between">
                      <span className="text-sm">Avg Buy Price:</span>
                      <span className="font-mono">
                        ${portfolio[selectedCrypto.symbol].avgPrice.toFixed(2)}
                      </span>
                    </div>
                  </div>
                ) : (
                  <div className="text-slate-500 text-sm italic">
                    You don't own any {selectedCrypto.symbol} yet
                  </div>
                )}
              </div>
            </div>
          )}

          <div className="grid grid-cols-2 gap-2">
            <Button
              variant={tradeType === "buy" ? "default" : "outline"}
              onClick={() => setTradeType("buy")}
              className="h-12 lg:h-10"
              disabled={isExecutingTrade}
            >
              Buy
            </Button>
            <Button
              variant={tradeType === "sell" ? "default" : "outline"}
              onClick={() => setTradeType("sell")}
              className="h-12 lg:h-10"
              disabled={isExecutingTrade}
            >
              Sell
            </Button>
          </div>

          <div>
            <label className="block text-sm font-medium mb-2">
              Amount ({selectedCrypto?.symbol || "Select crypto"})
            </label>
            <Input
              type="number"
              value={tradeAmount}
              onChange={(e) => setTradeAmount(e.target.value)}
              placeholder="Enter amount"
              className="bg-slate-700 border-slate-600 h-12 lg:h-10 text-lg lg:text-base"
              disabled={!selectedCrypto || isExecutingTrade}
            />
          </div>

          {selectedCrypto && tradeAmount && (
            <div className="p-3 lg:p-4 bg-slate-700 rounded">
              <div className="flex justify-between text-lg lg:text-base">
                <span>Total Cost:</span>
                <span className="font-bold">
                  ${(parseFloat(tradeAmount) * selectedCrypto.price).toFixed(2)}
                </span>
              </div>
            </div>
          )}

          <Button
            onClick={executeTrade}
            disabled={!selectedCrypto || !tradeAmount || isExecutingTrade}
            className="w-full bg-green-600 hover:bg-green-700 h-12 lg:h-10 text-lg lg:text-base disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {isExecutingTrade
              ? "Processing..."
              : `Execute ${tradeType === "buy" ? "Buy" : "Sell"} Order`}
          </Button>

          {/* Trade Message Display */}
          {tradeMessage && (
            <div
              className={`p-4 rounded-lg border ${
                tradeMessage.type === "success"
                  ? "bg-green-900/20 border-green-600 text-green-300"
                  : "bg-red-900/20 border-red-600 text-red-300"
              }`}
            >
              <div className="font-semibold text-sm">{tradeMessage.title}</div>
              <div className="text-sm mt-1">{tradeMessage.message}</div>
            </div>
          )}

          {!selectedCrypto && (
            <div className="text-slate-400 text-center py-4">
              Select a cryptocurrency to start trading
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  );
};

export default TradeTab;
