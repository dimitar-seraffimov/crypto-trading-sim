import React from "react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";
import { Badge } from "./ui/badge";

const HistoryTab = ({ transactions }) => {
  return (
    <Card className="bg-slate-800 border-slate-700">
      <CardHeader>
        <CardTitle className="text-lg lg:text-xl">
          Transaction History
        </CardTitle>
      </CardHeader>
      <CardContent>
        {transactions.length === 0 ? (
          <div className="text-center py-8 text-slate-400">
            No transactions yet. Start trading to see your history!
          </div>
        ) : (
          <>
            {/* Mobile: Card layout */}
            <div className="lg:hidden space-y-3">
              {transactions.map((transaction) => (
                <div
                  key={transaction.id}
                  className="bg-slate-700/50 rounded-lg p-4"
                >
                  <div className="flex justify-between items-start mb-2">
                    <div>
                      <Badge
                        variant={
                          transaction.type === "BUY" ||
                          transaction.type === "buy"
                            ? "default"
                            : "secondary"
                        }
                        className={`${
                          transaction.type === "BUY" ||
                          transaction.type === "buy"
                            ? "bg-green-600"
                            : "bg-red-600"
                        } mb-1`}
                      >
                        {transaction.type.toUpperCase()}
                      </Badge>
                      <div className="font-semibold">{transaction.symbol}</div>
                    </div>
                    <div className="text-right">
                      <div className="font-bold">
                        ${transaction.total.toFixed(2)}
                      </div>
                      <div className="text-sm text-slate-400">
                        {transaction.quantity.toFixed(6)}
                      </div>
                    </div>
                  </div>
                  <div className="text-xs text-slate-400">
                    {transaction.timestamp.toLocaleString()}
                  </div>
                </div>
              ))}
            </div>

            {/* Desktop: Table layout */}
            <div className="hidden lg:block overflow-x-auto">
              <table className="w-full">
                <thead>
                  <tr className="border-b border-slate-700">
                    <th className="text-left p-3">Date</th>
                    <th className="text-left p-3">Type</th>
                    <th className="text-left p-3">Asset</th>
                    <th className="text-right p-3">Quantity</th>
                    <th className="text-right p-3">Price</th>
                    <th className="text-right p-3">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {transactions.map((transaction) => (
                    <tr
                      key={transaction.id}
                      className="border-b border-slate-700/50"
                    >
                      <td className="p-3 text-sm">
                        {transaction.timestamp.toLocaleString()}
                      </td>
                      <td className="p-3">
                        <Badge
                          variant={
                            transaction.type === "BUY" ||
                            transaction.type === "buy"
                              ? "default"
                              : "secondary"
                          }
                          className={
                            transaction.type === "BUY" ||
                            transaction.type === "buy"
                              ? "bg-green-600"
                              : "bg-red-600"
                          }
                        >
                          {transaction.type.toUpperCase()}
                        </Badge>
                      </td>
                      <td className="p-3 font-semibold">
                        {transaction.symbol}
                      </td>
                      <td className="text-right p-3">
                        {transaction.quantity.toFixed(6)}
                      </td>
                      <td className="text-right p-3">
                        ${transaction.price.toFixed(2)}
                      </td>
                      <td className="text-right p-3">
                        ${transaction.total.toFixed(2)}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  );
};

export default HistoryTab;
