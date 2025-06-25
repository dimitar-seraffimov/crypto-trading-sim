import React from "react";
import {
  DollarSign,
  BarChart3,
  History,
  TrendingUp,
  TrendingDown,
  Target,
} from "lucide-react";
import { Card, CardContent, CardHeader, CardTitle } from "./ui/card";

const StatsCards = ({
  balance,
  portfolioValue,
  transactionsLength,
  totalPnL,
  totalPnLPercentage,
  totalInvested,
  realizedPnL,
  unrealizedPnL,
}) => {
  // profit or loss
  const isProfit = totalPnL >= 0;
  const totalValue = balance + portfolioValue;

  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6 mb-6 lg:mb-8">
      {/* Cash Balance */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader className="pb-2 lg:pb-3">
          <CardTitle className="flex items-center gap-2 text-green-400 text-sm lg:text-base">
            <DollarSign className="w-4 h-4 lg:w-5 lg:h-5" />
            Cash Balance
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="text-xl lg:text-2xl font-bold">
            ${balance.toFixed(2)}
          </div>
        </CardContent>
      </Card>

      {/* Portfolio Value */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader className="pb-2 lg:pb-3">
          <CardTitle className="flex items-center gap-2 text-blue-400 text-sm lg:text-base">
            <BarChart3 className="w-4 h-4 lg:w-5 lg:h-5" />
            Portfolio Value
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="text-xl lg:text-2xl font-bold">
            ${portfolioValue.toFixed(2)}
          </div>
          <div className="text-xs text-slate-400 mt-1">
            Total: ${totalValue.toFixed(2)}
          </div>
        </CardContent>
      </Card>

      {/* Total P&L */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader className="pb-2 lg:pb-3">
          <CardTitle
            className={`flex items-center gap-2 text-sm lg:text-base ${
              isProfit ? "text-green-400" : "text-red-400"
            }`}
          >
            {isProfit ? (
              <TrendingUp className="w-4 h-4 lg:w-5 lg:h-5" />
            ) : (
              <TrendingDown className="w-4 h-4 lg:w-5 lg:h-5" />
            )}
            Total P&L
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div
            className={`text-xl lg:text-2xl font-bold ${
              isProfit ? "text-green-400" : "text-red-400"
            }`}
          >
            {isProfit ? "+" : ""}${totalPnL.toFixed(2)}
          </div>
          <div
            className={`text-xs mt-1 ${
              isProfit ? "text-green-400" : "text-red-400"
            }`}
          >
            {totalPnLPercentage >= 0 ? "+" : ""}
            {totalPnLPercentage.toFixed(2)}%
          </div>
        </CardContent>
      </Card>

      {/* Trading Activity */}
      <Card className="bg-slate-800 border-slate-700">
        <CardHeader className="pb-2 lg:pb-3">
          <CardTitle className="flex items-center gap-2 text-purple-400 text-sm lg:text-base">
            <History className="w-4 h-4 lg:w-5 lg:h-5" />
            Trading Activity
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="text-xl lg:text-2xl font-bold">
            {transactionsLength}
          </div>
          <div className="text-xs text-slate-400 mt-1">Total Trades</div>
        </CardContent>
      </Card>

      {/* Detailed P&L Breakdown - Centered below other cards */}
      <Card className="bg-slate-800 border-slate-700 sm:col-span-2 lg:col-start-2 lg:col-span-2">
        <CardHeader className="pb-2 lg:pb-3">
          <CardTitle className="flex items-center gap-2 text-amber-400 text-sm lg:text-base">
            <Target className="w-4 h-4 lg:w-5 lg:h-5" />
            P&L Breakdown
          </CardTitle>
        </CardHeader>
        <CardContent className="pt-0">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <div className="text-xs text-slate-400 mb-1">Realized P&L</div>
              <div
                className={`text-sm font-semibold ${
                  realizedPnL >= 0 ? "text-green-400" : "text-red-400"
                }`}
              >
                {realizedPnL >= 0 ? "+" : ""}${realizedPnL.toFixed(2)}
              </div>
            </div>
            <div>
              <div className="text-xs text-slate-400 mb-1">Unrealized P&L</div>
              <div
                className={`text-sm font-semibold ${
                  portfolioValue > 0 && unrealizedPnL >= 0
                    ? "text-green-400"
                    : portfolioValue > 0 && unrealizedPnL < 0
                    ? "text-red-400"
                    : "text-slate-400"
                }`}
              >
                {portfolioValue > 0
                  ? `${unrealizedPnL >= 0 ? "+" : ""}$${unrealizedPnL.toFixed(
                      2
                    )}`
                  : "$0.00"}
              </div>
            </div>
            <div>
              <div className="text-xs text-slate-400 mb-1">Total Invested</div>
              <div className="text-sm font-semibold text-white">
                ${totalInvested.toFixed(2)}
              </div>
            </div>
            <div>
              <div className="text-xs text-slate-400 mb-1">Available Cash</div>
              <div className="text-sm font-semibold text-white">
                ${balance.toFixed(2)}
              </div>
            </div>
          </div>
        </CardContent>
      </Card>
    </div>
  );
};

export default StatsCards;
