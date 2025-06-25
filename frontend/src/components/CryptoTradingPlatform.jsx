import React, { useState, useEffect } from "react";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "./ui/tabs";
import { toast } from "../hooks/use-toast";

// Import separated components
import Header from "./Header";
import StatsCards from "./StatsCards";
import MarketTab from "./MarketTab";
import TradeTab from "./TradeTab";
import PortfolioTab from "./PortfolioTab";
import HistoryTab from "./HistoryTab";

// API service for backend communication
import apiService from "../services/api";

const CryptoTradingPlatform = () => {
  // UI State only - NO business logic state
  const [cryptos, setCryptos] = useState([]);
  const [accountData, setAccountData] = useState(null);
  const [transactions, setTransactions] = useState([]);
  const [selectedCrypto, setSelectedCrypto] = useState(null);
  const [tradeAmount, setTradeAmount] = useState("");
  const [tradeType, setTradeType] = useState("buy");
  const [loading, setLoading] = useState(true);
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false);
  const [activeTab, setActiveTab] = useState("market");
  const [tradeMessage, setTradeMessage] = useState(null);
  const [isExecutingTrade, setIsExecutingTrade] = useState(false);

  // Static user ID for demo - in real app, this would come from authentication
  const userId = "demo-user";

  // Load initial data from backend
  useEffect(() => {
    loadInitialData();
    const cleanup = setupRealTimePriceUpdates();

    // Cleanup on component unmount
    return () => {
      if (cleanup) cleanup();
      if (window.pricePollingInterval) {
        clearInterval(window.pricePollingInterval);
        window.pricePollingInterval = null;
      }
    };
  }, []);

  const loadInitialData = async () => {
    try {
      setLoading(true);

      // Load cryptocurrency prices from backend
      console.log("Fetching crypto prices...");
      const prices = await apiService.getCryptoPrices();
      console.log("Received prices:", prices);
      console.log("Prices type:", typeof prices);
      console.log("Is array:", Array.isArray(prices));
      setCryptos(prices);

      // Load account data from backend
      const accountResponse = await apiService.getAccount();
      console.log("Account response:", accountResponse);

      // Convert portfolio array to object for backward compatibility
      const portfolioArray = accountResponse.account.portfolio || [];
      const portfolioObject = {};
      portfolioArray.forEach((holding) => {
        portfolioObject[holding.symbol] = holding;
      });

      const accountData = {
        ...accountResponse.account,
        portfolio: portfolioObject,
      };

      setAccountData(accountData);

      // Load transaction history from backend
      const historyResponse = await apiService.getTransactionHistory();
      setTransactions(historyResponse.transactions);

      setLoading(false);
    } catch (error) {
      console.error("Error loading initial data:", error);
      toast({
        title: "Error",
        description: "Failed to load data from server",
        variant: "destructive",
      });
      setLoading(false);
    }
  };

  const setupRealTimePriceUpdates = () => {
    try {
      const eventSource = apiService.createPriceStream();

      eventSource.onmessage = (event) => {
        try {
          const priceData = JSON.parse(event.data);
          console.log("SSE received price data:", priceData);
          setCryptos(priceData);
        } catch (error) {
          console.error("Error parsing price update:", error);
        }
      };

      eventSource.onerror = (error) => {
        console.error("Price stream error:", error);
        console.log("Falling back to polling...");
        eventSource.close();

        // Fallback to periodic polling
        const interval = setInterval(async () => {
          try {
            console.log("Polling for price updates...");
            const prices = await apiService.getCryptoPrices();
            console.log("Polled prices:", prices);
            setCryptos(prices);
          } catch (error) {
            console.error("Error fetching prices:", error);
          }
        }, 5000); // Poll every 5 seconds for more frequent updates

        // Store interval ID to clean up later
        window.pricePollingInterval = interval;
      };

      // Cleanup function
      return () => {
        eventSource.close();
        if (window.pricePollingInterval) {
          clearInterval(window.pricePollingInterval);
          window.pricePollingInterval = null;
        }
      };
    } catch (error) {
      console.error("Error setting up real-time updates:", error);
      // Start polling immediately if SSE setup fails
      console.log("SSE setup failed, starting polling immediately...");
      const interval = setInterval(async () => {
        try {
          console.log("Polling for price updates (SSE failed)...");
          const prices = await apiService.getCryptoPrices();
          console.log("Polled prices (SSE failed):", prices);
          setCryptos(prices);
        } catch (error) {
          console.error("Error fetching prices:", error);
        }
      }, 5000);

      window.pricePollingInterval = interval;
    }
  };

  // Trading logic - ALL moved to backend, frontend just calls API
  const executeTrade = async () => {
    if (isExecutingTrade) return;
    setIsExecutingTrade(true);
    setTradeMessage(null);

    // Basic client-side validation
    if (!selectedCrypto || !tradeAmount) {
      setTradeMessage({
        type: "error",
        title: "Error",
        message: "Please select a cryptocurrency and enter an amount",
      });
      setIsExecutingTrade(false);
      return;
    }

    const amount = parseFloat(tradeAmount);
    if (amount <= 0) {
      setTradeMessage({
        type: "error",
        title: "Error",
        message: "Please enter a valid amount",
      });
      setIsExecutingTrade(false);
      return;
    }

    try {
      // Call backend API to execute trade
      const tradeRequest = {
        symbol: selectedCrypto.symbol,
        type: tradeType.toUpperCase(),
        quantity: amount,
        price: selectedCrypto.price,
      };

      const result = await apiService.executeTrade(tradeRequest);

      // Show success message
      setTradeMessage({
        type: "success",
        title: result.message || "Trade Successful",
        message: `${tradeType === "buy" ? "Bought" : "Sold"} ${amount} ${
          selectedCrypto.symbol
        } for $${
          result.trade?.total?.toFixed(2) ||
          (amount * selectedCrypto.price).toFixed(2)
        }`,
      });

      // Refresh account data and transaction history from backend
      const [accountResponse, historyResponse] = await Promise.all([
        apiService.getAccount(),
        apiService.getTransactionHistory(),
      ]);

      // Convert portfolio array to object for backward compatibility
      const portfolioArray = accountResponse.account.portfolio || [];
      const portfolioObject = {};
      portfolioArray.forEach((holding) => {
        portfolioObject[holding.symbol] = holding;
      });

      const accountData = {
        ...accountResponse.account,
        portfolio: portfolioObject,
      };

      setAccountData(accountData);
      setTransactions(historyResponse.transactions);
      setTradeAmount("");

      toast({
        title: "Trade Executed",
        description: `Successfully ${tradeType} ${amount} ${selectedCrypto.symbol}`,
      });
    } catch (error) {
      console.error("Trade execution failed:", error);
      setTradeMessage({
        type: "error",
        title: "Trade Failed",
        message: error.message || "An error occurred while executing the trade",
      });

      toast({
        title: "Trade Failed",
        description:
          error.message || "An error occurred while executing the trade",
        variant: "destructive",
      });
    } finally {
      setTimeout(() => {
        setTradeMessage(null);
        setIsExecutingTrade(false);
      }, 3000);
    }
  };

  // Account management - API calls only
  const resetAccount = async () => {
    try {
      await apiService.resetAccount();

      // Refresh data from backend
      const [accountResponse, historyResponse] = await Promise.all([
        apiService.getAccount(),
        apiService.getTransactionHistory(),
      ]);

      // Convert portfolio array to object for backward compatibility
      const portfolioArray = accountResponse.account.portfolio || [];
      const portfolioObject = {};
      portfolioArray.forEach((holding) => {
        portfolioObject[holding.symbol] = holding;
      });

      const accountData = {
        ...accountResponse.account,
        portfolio: portfolioObject,
      };

      setAccountData(accountData);
      setTransactions(historyResponse.transactions);
      setTradeMessage(null);

      toast({
        title: "Account Reset",
        description: "Your account has been reset to $10,000",
      });
    } catch (error) {
      console.error("Account reset failed:", error);
      toast({
        title: "Reset Failed",
        description: error.message || "Failed to reset account",
        variant: "destructive",
      });
    }
  };

  const goToTradeTab = (crypto) => {
    setSelectedCrypto(crypto);
    setActiveTab("trade");
    toast({
      title: "Crypto Selected",
      description: `Ready to trade ${crypto.name} (${crypto.symbol})`,
    });
  };

  // All calculations are now done in the backend
  // Frontend just displays the computed values from accountData

  // Loading state
  if (loading || !accountData) {
    return (
      <div className="min-h-screen bg-slate-900 flex items-center justify-center">
        <div className="text-white text-xl">Loading trading platform...</div>
      </div>
    );
  }

  // Main render - ALL data comes from backend
  return (
    <div className="min-h-screen bg-slate-900 text-white">
      <div className="w-full h-full">
        <Header
          totalValue={accountData.totalValue}
          mobileMenuOpen={mobileMenuOpen}
          setMobileMenuOpen={setMobileMenuOpen}
          resetAccount={resetAccount}
        />

        <div className="p-4 lg:p-6">
          <StatsCards
            balance={accountData.balance}
            portfolioValue={accountData.portfolioValue}
            transactionsLength={accountData.transactionCount}
            totalPnL={accountData.totalPnL}
            totalPnLPercentage={accountData.totalPnLPercentage}
            totalInvested={accountData.totalInvested}
            realizedPnL={accountData.realizedPnL}
            unrealizedPnL={accountData.unrealizedPnL}
          />

          <Tabs
            value={activeTab}
            onValueChange={setActiveTab}
            className="w-full"
          >
            <TabsList className="grid grid-cols-4 bg-slate-800 w-full p-1 gap-1">
              <TabsTrigger
                value="market"
                className="text-xs sm:text-sm px-2 py-4 min-h-[48px] rounded-md font-medium transition-all duration-200 data-[state=active]:bg-blue-600 data-[state=active]:text-white"
              >
                Market
              </TabsTrigger>
              <TabsTrigger
                value="trade"
                className="text-xs sm:text-sm px-2 py-4 min-h-[48px] rounded-md font-medium transition-all duration-200 data-[state=active]:bg-blue-600 data-[state=active]:text-white"
              >
                Trade
              </TabsTrigger>
              <TabsTrigger
                value="portfolio"
                className="text-xs sm:text-sm px-2 py-4 min-h-[48px] rounded-md font-medium transition-all duration-200 data-[state=active]:bg-blue-600 data-[state=active]:text-white"
              >
                Portfolio
              </TabsTrigger>
              <TabsTrigger
                value="history"
                className="text-xs sm:text-sm px-2 py-4 min-h-[48px] rounded-md font-medium transition-all duration-200 data-[state=active]:bg-blue-600 data-[state=active]:text-white"
              >
                History
              </TabsTrigger>
            </TabsList>

            <TabsContent value="market">
              <MarketTab cryptos={cryptos} goToTradeTab={goToTradeTab} />
            </TabsContent>

            <TabsContent value="trade">
              <TradeTab
                cryptos={cryptos}
                selectedCrypto={selectedCrypto}
                setSelectedCrypto={setSelectedCrypto}
                portfolio={accountData.portfolio}
                tradeType={tradeType}
                setTradeType={setTradeType}
                tradeAmount={tradeAmount}
                setTradeAmount={setTradeAmount}
                executeTrade={executeTrade}
                tradeMessage={tradeMessage}
                isExecutingTrade={isExecutingTrade}
              />
            </TabsContent>

            <TabsContent value="portfolio">
              <PortfolioTab
                portfolio={accountData.portfolio}
                cryptos={cryptos}
                goToTradeTab={goToTradeTab}
              />
            </TabsContent>

            <TabsContent value="history">
              <HistoryTab transactions={transactions} />
            </TabsContent>
          </Tabs>
        </div>
      </div>
    </div>
  );
};

export default CryptoTradingPlatform;
