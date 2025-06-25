import React from "react";
import { RefreshCcw, Menu, X } from "lucide-react";
import { Button } from "./ui/button";

const Header = ({
  totalValue,
  mobileMenuOpen,
  setMobileMenuOpen,
  resetAccount,
}) => {
  return (
    <>
      {/* Mobile Header */}
      <div className="lg:hidden bg-slate-800 border-b border-slate-700 p-4">
        <div className="flex justify-between items-center">
          <h1 className="text-xl font-bold bg-gradient-to-r from-blue-400 to-green-400 bg-clip-text text-transparent">
            Crypto Trade Simulator
          </h1>
          <div className="flex items-center gap-2">
            <div className="text-right">
              <div className="text-xs text-slate-400">Portfolio</div>
              <div className="text-sm font-bold text-green-400">
                ${totalValue.toFixed(0)}
              </div>
            </div>
            <Button
              variant="ghost"
              size="icon"
              onClick={() => setMobileMenuOpen(!mobileMenuOpen)}
              className="lg:hidden"
            >
              {mobileMenuOpen ? (
                <X className="h-5 w-5" />
              ) : (
                <Menu className="h-5 w-5" />
              )}
            </Button>
          </div>
        </div>
      </div>

      {/* Desktop Header */}
      <header className="hidden lg:block p-6 bg-slate-800 border-b border-slate-700">
        <div className="flex justify-between items-center">
          <h1 className="text-4xl font-bold bg-gradient-to-r from-blue-400 to-green-400 bg-clip-text text-transparent">
            Crypto Trade Simulator
          </h1>
          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="text-sm text-slate-400">
                Total Portfolio Value
              </div>
              <div className="text-3xl font-bold text-green-400">
                ${totalValue.toFixed(2)}
              </div>
            </div>
            <Button
              onClick={resetAccount}
              variant="outline"
              className="border-red-500 text-red-500 hover:bg-red-500 hover:text-white"
            >
              <RefreshCcw className="w-4 h-4 mr-2" />
              Reset Account
            </Button>
          </div>
        </div>
      </header>

      {/* Mobile Menu Overlay */}
      {mobileMenuOpen && (
        <div
          className="lg:hidden fixed inset-0 bg-slate-900/50 backdrop-blur-sm z-50"
          onClick={() => setMobileMenuOpen(false)}
        >
          <div className="bg-slate-800 w-64 h-full p-4">
            <div className="mb-6">
              <Button
                onClick={resetAccount}
                variant="outline"
                className="w-full border-red-500 text-red-500 hover:bg-red-500 hover:text-white"
              >
                <RefreshCcw className="w-4 h-4 mr-2" />
                Reset Account
              </Button>
            </div>
          </div>
        </div>
      )}
    </>
  );
};

export default Header;
