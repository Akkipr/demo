import { useEffect, useMemo, useState } from 'react';
import { createPortal } from 'react-dom';
import { LineChart } from '@mui/x-charts/LineChart';

import { backendUrl, numToMoney } from '../utils';

const intervalOptions = [
  { label: 'Past Week', value: 'week' },
  { label: 'Past Month', value: 'month' },
  { label: 'Past Quarter', value: 'quarter' },
  { label: 'Past Year', value: 'year' },
  { label: 'Past 5 Years', value: 'fiveyears' },
];

const futureOptions = [
  { label: '30 days', value: 30 },
  { label: '90 days', value: 90 },
  { label: '180 days', value: 180 },
  { label: '365 days', value: 365 },
];

export default function StockPerformance({ symbol, isOpen, onClose }) {
  const [interval, setInterval] = useState('year');
  const [futureDays, setFutureDays] = useState(30);
  const [historical, setHistorical] = useState([]);
  const [future, setFuture] = useState([]);
  const [stats, setStats] = useState(null);
  const [currentPrice, setCurrentPrice] = useState(null);
  const [status, setStatus] = useState({ loading: false, error: '' });

  useEffect(() => {
    if (isOpen && symbol) {
      fetchData(symbol);
    }
  }, [isOpen, symbol, interval, futureDays]);

  const fetchData = async (ticker) => {
    setStatus({ loading: true, error: '' });
    try {
      const [histRes, futureRes, statsRes, priceRes] = await Promise.all([
        fetch(`${backendUrl}/historicalprices?symbol=${ticker}&interval=${interval}`, {
          credentials: 'include',
        }),
        fetch(`${backendUrl}/futureprices?symbol=${ticker}&days=${futureDays}`, {
          credentials: 'include',
        }),
        fetch(`${backendUrl}/stockstatistics?symbol=${ticker}`, {
          credentials: 'include',
        }),
        fetch(`${backendUrl}/stockprice?symbol=${ticker}`, { credentials: 'include' }),
      ]);

      const histData = histRes.ok ? await histRes.json() : [];
      const futureData = futureRes.ok ? await futureRes.json() : [];
      const statsText = statsRes.ok ? await statsRes.text() : '';
      const priceJson = priceRes.ok ? await priceRes.json() : null;

      setHistorical(histData);
      setFuture(futureData);
      setStats(statsText);
      setCurrentPrice(priceJson?.found ? priceJson.price : null);
      setStatus({ loading: false, error: '' });
    } catch (err) {
      setStatus({ loading: false, error: 'Unable to load stock data' });
    }
  };

  const dataset = useMemo(() => {
    const data = [];
    historical.forEach((point) => {
      data.push({
        date: point.date,
        historical: point.price,
        future: null,
      });
    });
    future.forEach((point) => {
      data.push({
        date: point.date,
        historical: null,
        future: point.price,
      });
    });
    return data;
  }, [historical, future]);

  if (!isOpen) return null;

  return createPortal(
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
      onClick={onClose}
    >
      <div
        style={{
          backgroundColor: 'white',
          padding: '20px',
          borderRadius: '8px',
          width: '700px',
        }}
        onClick={(e) => e.stopPropagation()}
      >
        <h2 style={{ textAlign: 'center', margin: '0 0 20px' }}>{symbol}</h2>

        <div style={{ display: 'flex', gap: '15px', marginBottom: '15px' }}>
          <select value={interval} onChange={(e) => setInterval(e.target.value)}>
            {intervalOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
          <select
            value={futureDays}
            onChange={(e) => setFutureDays(Number(e.target.value))}
          >
            {futureOptions.map((opt) => (
              <option key={opt.value} value={opt.value}>
                {opt.label}
              </option>
            ))}
          </select>
        </div>

        {status.error && <p style={{ color: 'red' }}>{status.error}</p>}

        <div style={{ display: 'flex', gap: '20px' }}>
          <div style={{ width: '170px' }}>
            <p>
              <strong>Current Value:</strong>{' '}
              {currentPrice != null ? `$${numToMoney(currentPrice)}` : 'N/A'}
            </p>
            <pre
              style={{
                whiteSpace: 'pre-wrap',
                background: '#f8f8f8',
                padding: '10px',
                borderRadius: '6px',
                maxHeight: '160px',
                overflowY: 'auto',
              }}
            >
              {stats || 'No statistics available'}
            </pre>
          </div>

          <div style={{ flex: 1 }}>
            <LineChart
              height={300}
              dataset={dataset}
              xAxis={[
                {
                  dataKey: 'date',
                  scaleType: 'point',
                },
              ]}
              series={[
                {
                  dataKey: 'historical',
                  label: 'Historical',
                  color: '#1976d2',
                  connectNulls: false,
                  showMark: false,
                },
                {
                  dataKey: 'future',
                  label: 'Predicted',
                  color: '#9c27b0',
                  showMark: false,
                  strokeDasharray: '5 5',
                },
              ]}
              loading={status.loading}
            />
          </div>
        </div>
      </div>
    </div>,
    document.body
  );
}
