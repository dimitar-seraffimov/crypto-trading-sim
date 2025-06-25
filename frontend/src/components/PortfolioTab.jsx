import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

const PortfolioTab = ({ portfolio, cryptos, goToTradeTab }) => {
  return (
    <Card className="bg-slate-800 border-slate-700">
      <CardHeader>
        <CardTitle className="text-lg lg:text-xl">Your Portfolio</CardTitle>
      </CardHeader>
      <CardContent>
        {Object.keys(portfolio).length === 0 ? (
          <div className="text-center py-8 text-slate-400">
            No holdings yet. Start trading to build your portfolio!
          </div>
        ) : (
          <>
            {/* Mobile: Card layout */}
            <div className="lg:hidden space-y-3">
              {Object.entries(portfolio).map(([symbol, holding]) => {
                const crypto = cryptos.find((c) => c.symbol === symbol);
                const currentValue = crypto
                  ? crypto.price * holding.quantity
                  : 0;
                const pnl = currentValue - holding.totalInvested;
                const pnlPercentage = (pnl / holding.totalInvested) * 100;

                return (
                  <div
                    key={symbol}
                    className="bg-slate-700/50 rounded-lg p-4 cursor-pointer hover:bg-slate-700 transition-colors border border-transparent hover:border-blue-500"
                    onClick={() => crypto && goToTradeTab(crypto)}
                  >
                    <div className="flex justify-between items-start mb-2">
                      <div>
                        <div className="font-semibold text-lg">{symbol}</div>
                        <div className="text-xs text-blue-400">
                          Click to trade →
                        </div>
                      </div>
                      <div className="text-right">
                        <div className="font-bold">
                          ${currentValue.toFixed(2)}
                        </div>
                        <div
                          className={`text-sm ${
                            pnl >= 0 ? "text-green-400" : "text-red-400"
                          }`}
                        >
                          {pnl >= 0 ? "+" : ""}${pnl.toFixed(2)} (
                          {pnlPercentage.toFixed(2)}%)
                        </div>
                      </div>
                    </div>
                    <div className="grid grid-cols-2 gap-4 text-sm text-slate-400">
                      <div>
                        <div>Quantity: {holding.quantity.toFixed(6)}</div>
                        <div>Avg Price: ${holding.avgPrice.toFixed(2)}</div>
                      </div>
                      <div className="text-right">
                        <div>Current: ${crypto?.price.toFixed(2) || "N/A"}</div>
                      </div>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Desktop: Table layout */}
            <div className="hidden lg:block overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-700">
                    <th className="text-left p-3">Asset</th>
                    <th className="text-right p-3">Quantity</th>
                    <th className="text-right p-3">Avg Price</th>
                    <th className="text-right p-3">Current Price</th>
                    <th className="text-right p-3">Value</th>
                    <th className="text-right p-3">P&L</th>
                  </tr>
                </thead>
                <tbody>
                  {Object.entries(portfolio).map(([symbol, holding]) => {
                    const crypto = cryptos.find((c) => c.symbol === symbol);
                    const currentValue = crypto
                      ? crypto.price * holding.quantity
                      : 0;
                    const pnl = currentValue - holding.totalInvested;
                    const pnlPercentage = (pnl / holding.totalInvested) * 100;

                    return (
                      <tr
                        key={symbol}
                        className="border-b border-slate-700/50 hover:bg-slate-700/30 cursor-pointer transition-colors"
                        onClick={() => crypto && goToTradeTab(crypto)}
                        title="Click to trade this cryptocurrency"
                      >
                        <td className="p-3 font-semibold">
                          <div className="flex items-center gap-2">
                            {symbol}
                            <span className="text-xs text-blue-400">→</span>
                          </div>
                        </td>
                        <td className="text-right p-3">
                          {holding.quantity.toFixed(6)}
                        </td>
                        <td className="text-right p-3">
                          ${holding.avgPrice.toFixed(2)}
                        </td>
                        <td className="text-right p-3">
                          ${crypto?.price.toFixed(2) || "N/A"}
                        </td>
                        <td className="text-right p-3">
                          ${currentValue.toFixed(2)}
                        </td>
                        <td className="text-right p-3">
                          <span
                            className={
                              pnl >= 0 ? "text-green-400" : "text-red-400"
                            }
                          >
                            ${pnl.toFixed(2)} ({pnlPercentage.toFixed(2)}%)
                          </span>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default PortfolioTab;
