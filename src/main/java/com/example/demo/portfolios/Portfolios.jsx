import { useEffect, useMemo, useState } from 'react';

import '../style.css';
import StockPerformance from '../stock-performance/StockPerformance';
import {
  backendUrl,
  GreenUpArrow,
  numToMoney,
  RedDownArrow,
} from '../utils';

export default function PortfoliosPage() {
  const [portfolios, setPortfolios] = useState([]);
  const [selectedPortfolioId, setSelectedPortfolioId] = useState(null);
  const [overview, setOverview] = useState(null);
  const [transfers, setTransfers] = useState([]);
  const [statusMessage, setStatusMessage] = useState('');
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [transferType, setTransferType] = useState('Deposit');
  const [performanceModal, setPerformanceModal] = useState({
    open: false,
    symbol: '',
  });

  useEffect(() => {
    fetchPortfolios(); // initial fetch of portfolios
  }, []);

  const selectedPortfolio = useMemo(
    () => portfolios.find((p) => p.portfolioId === selectedPortfolioId) ?? null,
    [portfolios, selectedPortfolioId]
  );

  useEffect(() => {
    if (selectedPortfolioId != null) {
      fetchOverview(selectedPortfolioId);
      fetchTransfers(selectedPortfolioId);
    }}, [selectedPortfolioId]);

  const fetchPortfolios = async () => {
    try {
      const res = await fetch(`${backendUrl}/portfolios`, {
        credentials: 'include',
      });
      if (!res.ok) {
        throw new Error('Unable to load portfolios');
      }
      const data = await res.json();
      setPortfolios(data);
      if (data.length && !selectedPortfolioId) {
        setSelectedPortfolioId(data[0].portfolioId);
      }
      if (!data.length) {
        setSelectedPortfolioId(null);
        setOverview(null);
        setTransfers([]);
      }
    } catch (err) {
      setStatusMessage(err.message);
    }
  };

  const fetchOverview = async (portfolioId) => {
    const res = await fetch(
      `${backendUrl}/portfoliooverview?portfolioId=${portfolioId}`,
      { credentials: 'include' }
    );
    if (res.ok) {
      setOverview(await res.json());
    }
  };

  const fetchTransfers = async (portfolioId) => {
    const res = await fetch(
      `${backendUrl}/transferhistory?portfolioId=${portfolioId}`,
      { credentials: 'include' }
    );
    if (res.ok) {
      setTransfers(await res.json());
    }
  };

  const handleAddPortfolio = async () => {
    const name = window.prompt('Portfolio name?');
    if (!name) return;

    const res = await fetch(
      `${backendUrl}/createportfolio?portfolioName=${encodeURIComponent(name)}`,
      { method: 'POST', credentials: 'include' }
    );
    setStatusMessage(await res.text());
    fetchPortfolios();
  };

  const handleRemovePortfolio = async () => {
    if (!selectedPortfolioId) return;
    if (!window.confirm('Remove selected portfolio?')) return;

    const res = await fetch(
      `${backendUrl}/removeportfolio?portfolioId=${selectedPortfolioId}`,
      { method: 'POST', credentials: 'include' });
    setStatusMessage(await res.text());
    fetchPortfolios();
  };

  const openTransferModal = (type) => {
    setTransferType(type);
    setIsModalOpen(true);
  };

  const handleAction = async (payload) => {
    if (!selectedPortfolioId) return;
    let url = '';
    const { cashAmount, stockSymbol, stockAmount } = payload;

    switch (payload.type) {
      case 'Deposit':
        url = `${backendUrl}/depositcash?portfolioId=${selectedPortfolioId}&amount=${cashAmount}`;
        break;
      case 'Withdraw':
        url = `${backendUrl}/withdrawcash?portfolioId=${selectedPortfolioId}&amount=${cashAmount}`;
        break;
      case 'Buy Stock':
        url = `${backendUrl}/buystock?portfolioId=${selectedPortfolioId}&symbol=${encodeURIComponent(stockSymbol)}&shares=${stockAmount}`;
        break;
      case 'Sell Stock':
        url = `${backendUrl}/sellstock?portfolioId=${selectedPortfolioId}&symbol=${encodeURIComponent(stockSymbol)}&shares=${stockAmount}`;
        break;
      default:
        return;
    }

    const res = await fetch(url, { method: 'POST', credentials: 'include' });
    const text = await res.text();
    setStatusMessage(text);
    setIsModalOpen(false);
    fetchOverview(selectedPortfolioId);
    fetchTransfers(selectedPortfolioId);
  };

  return (
    <div style={{ display: 'flex' }}>
      <div
        style={{
          width: '25%',
          borderRight: '1px solid #ccc',
          padding: '20px',
          backgroundColor: '#f9f9f9',
        }}
      >
        <h2>My Portfolios</h2>

        <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
          {portfolios.map((item) => (
            <button
              style={{
                padding: '10px',
                border: '1px solid #ddd',
                background:
                  item.portfolioId === selectedPortfolioId ? '#eee' : 'white',
                cursor: 'pointer',
              }}
              onClick={() => setSelectedPortfolioId(item.portfolioId)}
              key={item.portfolioId}
            >
              {item.portfolioName}
            </button>
          ))}
        </div>

        <div
          style={{ marginTop: '20px', display: 'flex', gap: '10px', flexWrap: 'wrap' }}
        >
          <button onClick={handleAddPortfolio}>Add Portfolio</button>
          <button onClick={handleRemovePortfolio}>Remove Selected</button>
        </div>

        {statusMessage && (
          <p style={{ marginTop: '15px', color: '#555' }}>{statusMessage}</p>
        )}
      </div>

      <RightSide
        portfolio={selectedPortfolio}
        overview={overview}
        transferHistory={transfers}
        onOpenModal={openTransferModal}
        onAction={handleAction}
        isModalOpen={isModalOpen}
        transferType={transferType}
        closeModal={() => setIsModalOpen(false)}
        onOpenPerformance={(symbol) =>
          setPerformanceModal({ open: true, symbol })
        }
      />

      <StockPerformance
        symbol={performanceModal.symbol}
        isOpen={performanceModal.open}
        onClose={() => setPerformanceModal({ open: false, symbol: '' })}
      />
    </div>
  );
}

function RightSide({
  portfolio,
  overview,
  transferHistory,
  onOpenModal,
  isModalOpen,
  transferType,
  closeModal,
  onAction,
  onOpenPerformance,
}) {
  const holdings = overview?.holdings ?? [];
  const cashBalance = overview?.cashBalance ?? 0;
  const holdingsMap = useMemo(() => {
    const map = {};
    holdings.forEach((h) => {
      map[h.symbol] = h.shareCount;
    });
    return map;
  }, [holdings]);

  if (!portfolio || !overview) {
    return (
      <div style={{ width: '75%', padding: '20px' }}>
        Select a portfolio to view details.
      </div>
    );
  }

  return (
    <div style={{ width: '75%', padding: '20px', overflowY: 'auto' }}>
      <PopupModal
        isOpen={isModalOpen}
        onClose={closeModal}
        transferType={transferType}
        onConfirm={onAction}
        cashBalance={cashBalance}
        holdingsMap={holdingsMap}
      />

      <h1>{overview.portfolioName}</h1>
      <h3>{`💵 Cash Balance: $${numToMoney(overview.cashBalance)}`}</h3>
      <p>
        Total Market Value:{' '}
        <strong>${numToMoney(overview.totalMarketValue)}</strong> | Net Worth:{' '}
        <strong>${numToMoney(overview.totalValue)}</strong>
      </p>

      <div style={{ marginBottom: '30px' }}>
        <h3>Stock Holdings</h3>
        <div style={{ display: 'flex', flexDirection: 'column', gap: '5px' }}>
          <div
            style={{
              display: 'flex',
              justifyContent: 'space-between',
              fontWeight: 'bold',
              borderBottom: '2px solid #eee',
              padding: '5px',
            }}
          >
            <span>Holding</span>
            <span>Est. Market Value</span>
          </div>
          {holdings.map((item) => (
            <div
              key={item.symbol}
              style={{
                display: 'flex',
                justifyContent: 'space-between',
                borderBottom: '1px solid #eee',
                padding: '10px 5px',
              }}
            >
              <span>
                <button onClick={() => onOpenPerformance(item.symbol)}>
                  {item.symbol}
                </button>{' '}
                - {item.shareCount} Share{item.shareCount === 1 ? '' : 's'}
              </span>
              <span>{'$' + numToMoney(item.marketValue)}</span>
            </div>
          ))}
          {!holdings.length && <p>No holdings yet.</p>}
        </div>
      </div>

      <div style={{ marginBottom: '30px' }}>
        <h3>Manage Funds & Stocks</h3>
        <div style={{ display: 'flex', gap: '10px', marginBottom: '15px' }}>
          <button onClick={() => onOpenModal('Deposit')}>Deposit Cash</button>
          <button onClick={() => onOpenModal('Withdraw')}>Withdraw Cash</button>
          <button onClick={() => onOpenModal('Buy Stock')}>Buy Stock</button>
          <button onClick={() => onOpenModal('Sell Stock')}>Sell Stock</button>
        </div>
      </div>

      <div>
        <h3>Transfer History</h3>
        <div style={{ border: '1px solid #eee' }}>
          {transferHistory.map((transfer) => (
            <div
              key={transfer.transferId}
              style={{ padding: '10px', borderBottom: '1px solid #eee' }}
            >
              {(transfer.transType === 'Deposit' ||
                transfer.transType === 'Sell Stock') && (
                <div>
                  [{transfer.transType}] +${numToMoney(transfer.amount)}{' '}
                  <GreenUpArrow /> {transfer.date}
                </div>
              )}
              {(transfer.transType === 'Withdraw' ||
                transfer.transType === 'Buy Stock') && (
                <div>
                  [{transfer.transType}] -${numToMoney(transfer.amount)}{' '}
                  <RedDownArrow /> {transfer.date}
                </div>
              )}
            </div>
          ))}
          {!transferHistory.length && <p style={{ padding: '10px' }}>No history.</p>}
        </div>
      </div>
    </div>
  );
}

function PopupModal({
  isOpen,
  onClose,
  transferType,
  onConfirm,
  cashBalance,
  holdingsMap,
}) {
  const [cashAmount, setCashAmount] = useState('');
  const [stockSymbol, setStockSymbol] = useState('');
  const [stockAmount, setStockAmount] = useState('');
  const [currentFetch, setCurrentFetch] = useState('');
  const [priceInfo, setPriceInfo] = useState({ loading: false, price: null });
  const [canConfirm, setCanConfirm] = useState(false);

  useEffect(() => {
    if (isOpen) {
      setCashAmount('');
      setStockSymbol('');
      setStockAmount('');
      setCurrentFetch('');
      setPriceInfo({ loading: false, price: null });
      setCanConfirm(false);
    }
  }, [isOpen, transferType]);

  useEffect(() => {
    if (!isOpen) return;
    if (transferType === 'Buy Stock' || transferType === 'Sell Stock') {
      fetchPrice(stockSymbol);
    }
  }, [stockSymbol, transferType, isOpen]);

  useEffect(() => {
    setCanConfirm(validate());
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [cashAmount, stockAmount, priceInfo, transferType]);

  const fetchPrice = async (symbol) => {
    if (!symbol) {
      setCurrentFetch('Enter a symbol');
      setPriceInfo({ loading: false, price: null });
      return;
    }

    setPriceInfo({ loading: true, price: null });
    const res = await fetch(
      `${backendUrl}/stockprice?symbol=${encodeURIComponent(symbol)}`,
      { credentials: 'include' }
    );

    if (!res.ok) {
      setCurrentFetch('Unable to fetch price');
      setPriceInfo({ loading: false, price: null });
      return;
    }

    const data = await res.json();
    if (!data.found) {
      setCurrentFetch('Symbol not found');
      setPriceInfo({ loading: false, price: null });
      return;
    }
    setCurrentFetch(`Last Close: $${numToMoney(data.price)}`);
    setPriceInfo({ loading: false, price: data.price });
  };

  const validate = () => {
    if (transferType === 'Deposit') {
      return Number(cashAmount) > 0;
    }
    if (transferType === 'Withdraw') {
      const value = Number(cashAmount);
      return value > 0 && value <= cashBalance;
    }
    if (transferType === 'Buy Stock') {
      const shares = Number(stockAmount);
      return (
        !!stockSymbol &&
        shares > 0 &&
        priceInfo.price != null &&
        shares * priceInfo.price <= cashBalance
      );
    }
    if (transferType === 'Sell Stock') {
      const shares = Number(stockAmount);
      return (
        !!stockSymbol &&
        shares > 0 &&
        holdingsMap[stockSymbol] &&
        holdingsMap[stockSymbol] >= shares
      );
    }
    return false;
  };

  const handleConfirm = () => {
    onConfirm({
      type: transferType,
      cashAmount: cashAmount ? Number(cashAmount) : null,
      stockSymbol,
      stockAmount: stockAmount ? Number(stockAmount) : null,
    });
  };

  if (!isOpen) return null;

  return (
    <div
      style={{
        position: 'fixed',
        top: 0,
        left: 0,
        right: 0,
        bottom: 0,
        backgroundColor: 'rgba(0,0,0,0.5)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'center',
        zIndex: 1000,
      }}
    >
      <div
        style={{
          backgroundColor: 'white',
          padding: '30px',
          borderRadius: '8px',
          boxShadow: '0 2px 10px rgba(0,0,0,0.2)',
          minWidth: '320px',
        }}
      >
        <h2>{transferType}</h2>

        {(transferType === 'Deposit' || transferType === 'Withdraw') && (
          <input
            type="number"
            placeholder="Amount"
            value={cashAmount}
            onChange={(e) => setCashAmount(e.target.value)}
            className="regular-input"
          />
        )}

        {(transferType === 'Buy Stock' || transferType === 'Sell Stock') && (
          <>
            <p style={{ minHeight: '24px' }}>{currentFetch}</p>
            <input
              type="text"
              placeholder="Symbol (e.g., AAPL)"
              value={stockSymbol}
              onChange={(e) => setStockSymbol(e.target.value.toUpperCase())}
              className="regular-input"
            />
            <input
              type="number"
              placeholder="Shares"
              min="1"
              value={stockAmount}
              onChange={(e) => setStockAmount(e.target.value)}
              className="regular-input"
            />
            {transferType === 'Sell Stock' && stockSymbol && holdingsMap[stockSymbol] && (
              <small>{`Owned: ${holdingsMap[stockSymbol]} shares`}</small>
            )}
          </>
        )}

        <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
          <button onClick={onClose}>Cancel</button>
          <button disabled={!canConfirm} onClick={handleConfirm}>
            Confirm
          </button>
        </div>
      </div>
    </div>
  );
}
