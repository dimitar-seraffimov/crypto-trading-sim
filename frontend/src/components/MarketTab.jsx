import React from "react";
import { TrendingUp, TrendingDown } from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";

const MarketTab = ({ cryptos, goToTradeTab }) => {
  // Safety check: ensure cryptos is an array
  const cryptoList = Array.isArray(cryptos) ? cryptos : [];

  if (cryptoList.length === 0) {
    return (
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader>
          <CardTitle className="text-lg lg:text-xl">
            Top 20 Cryptocurrencies
          </CardTitle>
        </CardHeader>
        <CardContent>
          <div className="text-center text-slate-400 py-8">
            Loading cryptocurrency data...
          </div>
        </CardContent>
      </Card>
    );
  }

  return (
    <Card className="bg-slate-800 border-slate-700">
      <CardHeader>
        <CardTitle className="text-lg lg:text-xl">
          Top 20 Cryptocurrencies
        </CardTitle>
      </CardHeader>
      <CardContent>
        {/* Mobile: Card layout, Desktop: Table layout */}
        <div className="lg:hidden space-y-3">
          {cryptoList.map((crypto) => (
            <div
              key={crypto.symbol}
              className="bg-slate-700/50 rounded-lg p-4 cursor-pointer hover:bg-slate-700/70 transition-colors"
              onClick={() => goToTradeTab && goToTradeTab(crypto)}
            >
              <div className="flex justify-between items-start mb-2">
                <div>
                  <div className="font-semibold text-white">
                    {crypto.name || crypto.symbol}
                  </div>
                  <div className="text-sm text-slate-400">{crypto.symbol}</div>
                </div>
                <div className="text-right">
                  <div className="font-mono text-white">
                    ${(crypto.price || 0).toFixed(2)}
                  </div>
                  <Badge
                    variant={
                      (crypto.change24hPercent || 0) >= 0
                        ? "default"
                        : "destructive"
                    }
                    className={`text-xs ${
                      (crypto.change24hPercent || 0) >= 0 ? "bg-green-500" : ""
                    }`}
                  >
                    {(crypto.change24hPercent || 0) >= 0 ? (
                      <TrendingUp className="w-3 h-3 mr-1" />
                    ) : (
                      <TrendingDown className="w-3 h-3 mr-1" />
                    )}
                    {(crypto.change24hPercent || 0).toFixed(2)}%
                  </Badge>
                </div>
              </div>
            </div>
          ))}
        </div>

        {/* Desktop: Table layout */}
        <div className="hidden lg:block overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="border-b border-slate-700">
                <th className="text-left p-3">Name</th>
                <th className="text-right p-3">Price</th>
                <th className="text-right p-3">24h Change</th>
                <th className="text-right p-3">Action</th>
              </tr>
            </thead>
            <tbody>
              {cryptoList.map((crypto) => (
                <tr
                  key={crypto.symbol}
                  className="border-b border-slate-700/50 hover:bg-slate-700/30"
                >
                  <td className="p-3">
                    <div>
                      <div className="font-semibold">
                        {crypto.name || crypto.symbol}
                      </div>
                      <div className="text-sm text-slate-400">
                        {crypto.symbol}
                      </div>
                    </div>
                  </td>
                  <td className="text-right p-3 font-mono">
                    ${(crypto.price || 0).toFixed(2)}
                  </td>
                  <td className="text-right p-3">
                    <Badge
                      variant={
                        (crypto.change24hPercent || 0) >= 0
                          ? "default"
                          : "destructive"
                      }
                      className={
                        (crypto.change24hPercent || 0) >= 0
                          ? "bg-green-500"
                          : ""
                      }
                    >
                      {(crypto.change24hPercent || 0) >= 0 ? (
                        <TrendingUp className="w-3 h-3 mr-1" />
                      ) : (
                        <TrendingDown className="w-3 h-3 mr-1" />
                      )}
                      {(crypto.change24hPercent || 0).toFixed(2)}%
                    </Badge>
                  </td>
                  <td className="text-right p-3">
                    <button
                      onClick={() => goToTradeTab && goToTradeTab(crypto)}
                      className="px-3 py-1 bg-blue-600 hover:bg-blue-700 rounded text-sm transition-colors"
                    >
                      Trade
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </CardContent>
    </Card>
  );
};

export default MarketTab;
