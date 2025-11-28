-- Portfolio System SQL Tables
-- Simplified: Foreign keys added directly to main tables instead of separate relationship tables
-- Based on the relational schema provided

-- 1. Portfolios table (Portfolio entity)
-- Portfolio(portfolio_ID, portfolio_name, balance)
-- ContainsPortfolio relationship: user_id is included directly in portfolios table
CREATE TABLE portfolios (
    portfolio_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    portfolio_name VARCHAR(20) NOT NULL,
    balance NUMERIC(15, 2) NOT NULL DEFAULT 0.00,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE
);

-- 2. Stock Holdings table (StockHolding entity)
-- StockHolding(holding_ID, share_count)
-- OwnsStockHolding relationship: portfolio_id is included directly
-- OwnsStock relationship: symbol is included directly
CREATE TABLE stock_holdings (
    holding_id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    share_count INTEGER NOT NULL CHECK (share_count > 0),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE,
    UNIQUE(portfolio_id, symbol)  -- One holding per stock per portfolio
);

-- 3. Transfers table (Transfer entity)
-- Transfer(transfer_ID, amount, date, trans_type, from_acc, to_acc)
-- Initiates relationship: portfolio_id is included directly
CREATE TABLE transfers (
    transfer_id BIGSERIAL PRIMARY KEY,
    portfolio_id BIGINT NOT NULL,
    amount NUMERIC(15, 2) NOT NULL,
    date DATE NOT NULL,
    trans_type VARCHAR(50) NOT NULL,
    from_acc VARCHAR(255),
    to_acc VARCHAR(255),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE
);

-- 4. Stocks Current table (Stock entity - for current prices)
-- Stock(symbol, current_price)
-- Note: This is separate from your existing stocks table which has historical data
CREATE TABLE stocks_current (
    symbol VARCHAR(10) PRIMARY KEY,
    current_price NUMERIC(15, 2) NOT NULL
);

-- 5. Stocks table (StockDayRange entity) - ALREADY EXISTS
-- StockDayRange(timestamp, open, high, low, close, volume)
-- InfoOfStock relationship: (symbol, timestamp) composite key
-- Note: This table already exists in your database with 600k+ rows
-- Expected structure:
-- CREATE TABLE stocks (
--     symbol VARCHAR(10) NOT NULL,
--     timestamp DATE NOT NULL,
--     open NUMERIC(15, 2),
--     high NUMERIC(15, 2),
--     low NUMERIC(15, 2),
--     close NUMERIC(15, 2),
--     volume BIGINT,
--     PRIMARY KEY (symbol, timestamp)
-- );
-- If your existing stocks table doesn't have a composite primary key, you may want to add:
-- ALTER TABLE stocks ADD PRIMARY KEY (symbol, timestamp);

-- 6. Stock statistics cache (per symbol & date range)
CREATE TABLE stock_statistics_cache (
    symbol VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    mean_return NUMERIC(20, 10),
    std_dev NUMERIC(20, 10),
    coefficient_of_variation NUMERIC(20, 10),
    beta NUMERIC(20, 10),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (symbol, start_date, end_date)
);

-- 7. Portfolio covariance cache (per portfolio/date range, per symbol pair)
CREATE TABLE portfolio_covariance_cache (
    portfolio_id BIGINT NOT NULL,
    symbol1 VARCHAR(10) NOT NULL,
    symbol2 VARCHAR(10) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    covariance NUMERIC(20, 10),
    correlation NUMERIC(20, 10),
    last_updated TIMESTAMP NOT NULL DEFAULT NOW(),
    PRIMARY KEY (portfolio_id, symbol1, symbol2, start_date, end_date),
    FOREIGN KEY (portfolio_id) REFERENCES portfolios(portfolio_id) ON DELETE CASCADE
);
