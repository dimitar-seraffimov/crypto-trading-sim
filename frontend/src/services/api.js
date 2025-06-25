const API_BASE_URL = "http://localhost:8080/api";

class ApiService {
  constructor() {
    this.baseURL = API_BASE_URL;
  }

  async request(endpoint, options = {}) {
    const url = `${this.baseURL}${endpoint}`;
    const config = {
      headers: {
        "Content-Type": "application/json",
        ...options.headers,
      },
      ...options,
    };

    try {
      console.log("Making API request to:", url);
      const response = await fetch(url, config);
      console.log("Response status:", response.status);
      console.log("Response ok:", response.ok);

      if (!response.ok) {
        const errorData = await response.json();
        console.error("API error response:", errorData);
        // Extract error message from backend response format
        const errorMessage =
          errorData.error ||
          errorData.message ||
          `HTTP error! status: ${response.status}`;
        throw new Error(errorMessage);
      }

      const data = await response.json();
      console.log("API response data:", data);
      return data;
    } catch (error) {
      console.error("API request failed:", error);
      throw error;
    }
  }

  // Market Data APIs
  async getCryptoPrices() {
    return this.request("/market/prices");
  }

  async getCryptoPrice(symbol) {
    return this.request(`/market/prices/${symbol}`);
  }

  // Trading APIs
  async executeTrade(tradeData) {
    return this.request("/simple-trading/trade", {
      method: "POST",
      body: JSON.stringify(tradeData),
    });
  }

  async getAccount(userId = null) {
    // In simplified mode, no userId needed
    return this.request("/simple-trading/account");
  }

  async resetAccount(userId = null) {
    // In simplified mode, no userId needed
    return this.request("/simple-trading/account/reset", {
      method: "POST",
    });
  }

  async getTransactionHistory(userId = null) {
    // In simplified mode, no userId needed
    return this.request("/simple-trading/transactions");
  }

  // Real-time price stream using Server-Sent Events
  createPriceStream() {
    const eventSource = new EventSource(`${this.baseURL}/market/prices/stream`);
    return eventSource;
  }
}

export default new ApiService();
