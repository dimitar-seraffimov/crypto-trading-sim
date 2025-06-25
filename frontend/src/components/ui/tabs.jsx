import React from "react";
import { cn } from "../../lib/utils";

const TabsContext = React.createContext();

const Tabs = React.forwardRef(
  (
    { className, defaultValue, value, onValueChange, children, ...props },
    ref
  ) => {
    const [selectedValue, setSelectedValue] = React.useState(
      defaultValue || value
    );

    const handleValueChange = (newValue) => {
      setSelectedValue(newValue);
      if (onValueChange) {
        onValueChange(newValue);
      }
    };

    React.useEffect(() => {
      if (value !== undefined) {
        setSelectedValue(value);
      }
    }, [value]);

    return (
      <TabsContext.Provider
        value={{ value: selectedValue, onValueChange: handleValueChange }}
      >
        <div ref={ref} className={cn("w-full", className)} {...props}>
          {children}
        </div>
      </TabsContext.Provider>
    );
  }
);
Tabs.displayName = "Tabs";

const TabsList = React.forwardRef(({ className, ...props }, ref) => (
  <div
    ref={ref}
    className={cn(
      "inline-flex h-16 items-center justify-center rounded-md bg-muted p-1 text-muted-foreground",
      className
    )}
    {...props}
  />
));
TabsList.displayName = "TabsList";

const TabsTrigger = React.forwardRef(
  ({ className, value, children, ...props }, ref) => {
    const context = React.useContext(TabsContext);

    return (
      <button
        ref={ref}
        className={cn(
          "inline-flex items-center justify-center whitespace-nowrap rounded-sm px-3 py-1.5 text-sm font-medium ring-offset-background transition-all focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-2 disabled:pointer-events-none disabled:opacity-50",
          context?.value === value
            ? "bg-background text-foreground shadow-sm"
            : "hover:bg-background/50",
          className
        )}
        onClick={() => context?.onValueChange(value)}
        {...props}
      >
        {children}
      </button>
    );
  }
);
TabsTrigger.displayName = "TabsTrigger";

const TabsContent = React.forwardRef(
  ({ className, value, children, ...props }, ref) => {
    const context = React.useContext(TabsContext);

    if (context?.value !== value) return null;

    return (
      <div
        ref={ref}
        className={cn(
          "mt-8 ring-offset-background focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring focus-visible:ring-offset-5",
          className
        )}
        {...props}
      >
        {children}
      </div>
    );
  }
);
TabsContent.displayName = "TabsContent";

export { Tabs, TabsList, TabsTrigger, TabsContent };
