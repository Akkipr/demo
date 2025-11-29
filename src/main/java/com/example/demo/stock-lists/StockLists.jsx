import React, { useEffect, useState } from 'react';
import StockPerformance from '../stock-performance/StockPerformance';
import { BigButtonStyle, EmptyListText, Title } from '../utils';
import AddNewStockModal from './AddNewStockModal';

// backendUrl is empty so the React code uses the same origin as the Spring Boot server
const backendUrl = '';

// Mock Data for Lists
const mockStockList = [
    { stock_list_id: 4, name: 'Tech Giants Watchlist', visibility: 'Public' },
    { stock_list_id: 5, name: 'Safe Dividends', visibility: 'Private' },
    { stock_list_id: 6, name: 'Friends Recommendations', visibility: 'Friends' },
] 

const mockStockHoldings = {
    4: [
        { holding_id: 1, symbol: 'AAPL', shares: 50 },
        { holding_id: 2, symbol: 'MSFT', shares: 30 },
    ],
    5: [
        { holding_id: 3, symbol: 'JNJ', shares: 20 },
        { holding_id: 4, symbol: 'PG', shares: 25 },
    ],
    6: [
        { holding_id: 5, symbol: 'GOOGL', shares: 15 },
        { holding_id: 6, symbol: 'TSLA', shares: 10 },
        { holding_id: 7, symbol: 'PG', shares: 1 },
    ],
}

const mockReviews = {
    4: [
        { id: 1, email: 'user1@example.com', text: 'Great tech stocks to watch!' },
        { id: 2, email: 'user2@example.com', text: 'AAPL and MSFT are solid picks.' },
        { id: 3, email: 'user3@example.com', text: 'Excellent curated list.' },
        { id: 4, email: 'user4@example.com', text: 'Very helpful for my portfolio.' },
        { id: 5, email: 'user5@example.com', text: 'Would recommend to others.' },
    ],
    5: [
        { id: 1, email: 'dividend@example.com', text: 'Perfect for passive income.' },
        { id: 2, email: 'investor@example.com', text: 'JNJ and PG never disappoint.' },
    ],
    6: [
        { id: 1, email: 'friend1@example.com', text: 'Love these recommendations!' },
        { id: 2, email: 'friend2@example.com', text: 'GOOGL and TSLA are my favorites.' },
    ],
}

export default function StockLists() {
    const [selectedListId, setSelectedListId] = useState(null);
    
    // Stuff required for "editing" or "local" changes
    // 🎯 Replace the first three "mock data" with the DB's stock lists and reviews 
    const [localStockHoldings, setLocalStockHoldings] = useState({});
    const [localStockLists, setLocalStockLists] = useState([]);
    const [localReviews, setLocalReviews] = useState({});  // Add this
    
    const [localVisibility, setLocalVisibility] = useState(localStockLists[0]?.visibility || '');
    
    const [hasChanges, setHasChanges] = useState(false);

    // Load all stock lists (including holdings & reviews) for the signed-in user
    useEffect(() => {
        async function loadLists() {
            try {
                const res = await fetch(`${backendUrl}/stocklists`);
                if (!res.ok) return;
                const payload = await res.json();

                const lists = payload.map(l => ({
                    stock_list_id: l.stock_list_id || l.stockListId,
                    name: l.name,
                    visibility: l.visibility
                }));

                const holdingsMap = {};
                const reviewsMap = {};
                payload.forEach(l => {
                    const id = l.stock_list_id || l.stockListId;
                    holdingsMap[id] = (l.holdings || []).map(h => ({
                        holding_id: h.holdingId || h.holding_id,
                        symbol: h.symbol,
                        shares: h.shareCount || h.shares || 0,
                        name: h.name || h.symbol
                    }));

                    reviewsMap[id] = (l.reviews || []).map(r => ({ id: r.reviewId || r.id, email: r.email, text: r.text }));
                });

                setLocalStockLists(lists);
                setLocalStockHoldings(holdingsMap);
                setLocalReviews(reviewsMap);

                if (lists.length) {
                    setSelectedListId(lists[0].stock_list_id);
                    setLocalVisibility(lists[0].visibility || '');
                }
            } catch (err) {
                console.error('Failed to load stock lists', err);
            }
        }
        loadLists();
    }, []);

    // development helper: attempt an auto-login (server will create a dev user if missing)
    async function ensureLoggedInDev() {
        try {
            const res = await fetch(`${backendUrl}/autologin?email=dev@example.com&name=Dev%20User&password=pass`);
            if (!res.ok) return false;
            const text = await res.text();
            console.log('autologin result:', text);
            // reload lists after login
            const r2 = await fetch(`${backendUrl}/stocklists`);
            if (!r2.ok) return true; // at least logged in
            const payload = await r2.json();
            const lists = payload.map(l => ({ stock_list_id: l.stock_list_id || l.stockListId, name: l.name, visibility: l.visibility }));
            setLocalStockLists(lists);
            const holdingsMap = {}; const reviewsMap = {};
            payload.forEach(l => {
                const id = l.stock_list_id || l.stockListId;
                holdingsMap[id] = (l.holdings || []).map(h => ({ holding_id: h.holdingId || h.holding_id, symbol: h.symbol, shares: h.shareCount || h.shares || 0, name: h.name || h.symbol }));
                reviewsMap[id] = (l.reviews || []).map(r => ({ id: r.reviewId || r.id, email: r.email, text: r.text }));
            });
            setLocalStockHoldings(holdingsMap);
            setLocalReviews(reviewsMap);
            return true;
        } catch (err) {
            console.error('autologin failed', err);
            return false;
        }
    }

    // When selecting a different list, reset local edits
    function selectList(listId) {
        if (listId === selectedListId) return;

        // Block switching if there are unsaved changes
        if (hasChanges) {
            window.alert('You have unsaved changes. Save or discard them first.');
            return;
        }

        const list = localStockLists.find(list => list.stock_list_id === listId);
        setSelectedListId(listId);
        setLocalVisibility(list?.visibility || '');
        // keep values already loaded in the maps (loaded on mount)
        setLocalStockHoldings(prev => ({ ...prev }));
        setLocalStockLists(prev => ([...prev]));
        setLocalReviews(prev => ({ ...prev }));

        // lastly...
        setHasChanges(false);
    }

    function updateLocalVisibility(newVisibility)
    {
        setLocalVisibility(newVisibility);
        setHasChanges(true);
    }

    // #region Stuff for editing our held shares
    async function updateStockHolding(holdingId, newShares) {
        try {
            const res = await fetch(`${backendUrl}/stocklists/holdings/${holdingId}?shares=${encodeURIComponent(newShares)}`, { method: 'PUT' });
            if (res.status === 401) { 
                const logged = await ensureLoggedInDev();
                if (logged) {
                    const retry = await fetch(`${backendUrl}/stocklists/holdings/${holdingId}?shares=${encodeURIComponent(newShares)}`, { method: 'PUT' });
                    if (retry.ok) {
                        const updated = await retry.json();
                        setLocalStockHoldings(prev => ({ ...prev, [selectedListId]: prev[selectedListId].map(h => (h.holding_id === holdingId || h.holdingId === holdingId) ? { ...h, shares: updated.shareCount || updated.shares || newShares } : h) }));
                        setHasChanges(false);
                        return;
                    }
                }
                window.alert('You must be logged in to update holdings.');
                return; }
            if (res.status === 403) { window.alert('Access denied when updating this holding.'); return; }
            if (!res.ok) throw new Error('update failed');
            const updated = await res.json();

            setLocalStockHoldings(prev => ({
                ...prev,
                [selectedListId]: prev[selectedListId].map(h =>
                    (h.holding_id === holdingId || h.holdingId === holdingId) ? { ...h, shares: updated.shareCount || updated.shares || newShares } : h
                )
            }));
            setHasChanges(false);
        } catch (err) {
            console.error('updateStockHolding failed', err);
            setLocalStockHoldings(prev => ({
                ...prev,
                [selectedListId]: prev[selectedListId].map(h =>
                    h.holding_id === holdingId ? { ...h, shares: newShares } : h
                )
            }));
            setHasChanges(true);
        }
    }
    
    async function addStockHolding(symbol, shares) {
        // if the selected list is still a temp local-only list, persist it first so we can POST holdings to a real ID
        let actualListId = selectedListId;
        if (typeof actualListId === 'string' && actualListId.startsWith('new_')) {
            const localList = localStockLists.find(l => l.stock_list_id === selectedListId);
            if (localList) {
                try {
                    const resCreate = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(localList.name)}`, { method: 'POST' });
                    if (resCreate.status === 401) {
                        const logged = await ensureLoggedInDev();
                        if (!logged) {
                            // cannot persist list — continue to add local holding only
                            console.warn('not logged in; new list remains local-only');
                        }
                        // retry create
                        const retry = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(localList.name)}`, { method: 'POST' });
                        if (retry.ok) {
                            const created = await retry.json();
                            const newId = created.stockListId || created.stock_list_id;
                            setLocalStockLists(prev => prev.map(p => p.stock_list_id === localList.stock_list_id ? { ...p, stock_list_id: newId } : p));
                            setLocalStockHoldings(prev => {
                                const next = { ...prev };
                                if (next[localList.stock_list_id]) {
                                    next[newId] = next[localList.stock_list_id];
                                    delete next[localList.stock_list_id];
                                }
                                return next;
                            });
                            setSelectedListId(newId);
                            actualListId = newId; // use this for subsequent POST below (setState is async)
                        }
                    } else if (resCreate.ok) {
                        const created = await resCreate.json();
                        const newId = created.stockListId || created.stock_list_id;
                        setLocalStockLists(prev => prev.map(p => p.stock_list_id === localList.stock_list_id ? { ...p, stock_list_id: newId } : p));
                        setLocalStockHoldings(prev => {
                            const next = { ...prev };
                            if (next[localList.stock_list_id]) {
                                next[newId] = next[localList.stock_list_id];
                                delete next[localList.stock_list_id];
                            }
                            return next;
                        });
                        setSelectedListId(newId);
                        actualListId = newId; // ensure local variable updated for this call
                    }
                } catch (err) {
                    console.error('failed to create list before adding holding', err);
                }
            }
        }
        try {
            const useListId = actualListId || selectedListId;
            // guard: don't attempt to POST if id is still a temporary local-only ID
            if (typeof useListId === 'string' && useListId.startsWith('new_')) {
                // list still not persisted on server — add as local-only holding and exit
                const newHoldingLocal = { holding_id: `new_${Date.now()}`, symbol: symbol, shares };
                setLocalStockHoldings(prev => ({ ...prev, [useListId]: [...(prev[useListId] || []), newHoldingLocal] }));
                setHasChanges(true);
                return;
            }

            const res = await fetch(`${backendUrl}/stocklists/${useListId}/holdings?symbol=${encodeURIComponent(symbol)}&shares=${encodeURIComponent(shares)}`, { method: 'POST' });
            if (res.status === 401) { 
                const logged = await ensureLoggedInDev();
                if (logged) {
                    // retry
                    const retry = await fetch(`${backendUrl}/stocklists/${selectedListId}/holdings?symbol=${encodeURIComponent(symbol)}&shares=${encodeURIComponent(shares)}`, { method: 'POST' });
                    if (retry.ok) {
                        const created = await retry.json();
                        const toAdd = { holding_id: created.holdingId || created.holding_id, symbol: created.symbol, shares: created.shareCount || created.shares || shares };
                        setLocalStockHoldings(prev => ({ ...prev, [selectedListId]: [...(prev[selectedListId] || []), toAdd] }));
                        setHasChanges(false);
                        return;
                    }
                }
                window.alert('You must be logged in to add a holding.');
                return; }
            if (res.status === 403) { window.alert('Access denied when adding to this stock list.'); return; }
            if (!res.ok) throw new Error('add failed');
            const created = await res.json();

            const toAdd = {
                holding_id: created.holdingId || created.holding_id,
                symbol: created.symbol,
                shares: created.shareCount || created.shares || shares
            };

            setLocalStockHoldings(prev => ({
                ...prev,
                [selectedListId]: [...(prev[selectedListId] || []), toAdd]
            }));
            setHasChanges(false);
        } catch (err) {
            console.error('addStockHolding failed', err);
            const newHolding = { holding_id: `new_${Date.now()}`, symbol: symbol, shares };
            setLocalStockHoldings(prev => ({ ...prev, [selectedListId]: [...(prev[selectedListId] || []), newHolding] }));
            setHasChanges(true);
        }
    }
    
    async function removeStockHolding(holdingId) {
        try {
            // If this is a temporary local id, remove locally only
            if (typeof holdingId === 'string' && holdingId.startsWith('new_')) {
                setLocalStockHoldings(prev => ({
                    ...prev,
                    [selectedListId]: prev[selectedListId].filter(h => h.holding_id !== holdingId)
                }));
                setHasChanges(true);
                return;
            }

            const res = await fetch(`${backendUrl}/stocklists/holdings/${holdingId}`, { method: 'DELETE' });
            if (!res.ok) throw new Error('delete failed');

            setLocalStockHoldings(prev => ({
                ...prev,
                [selectedListId]: prev[selectedListId].filter(h => h.holding_id !== holdingId)
            }));
            setHasChanges(false);
        } catch (err) {
            console.error('removeStockHolding failed', err);
            window.alert('Failed to remove holding');
        }
    }

    const hasStockHolding = (symbol) => {
        return localStockHoldings[selectedListId]?.some(h => h.symbol === symbol) || false;
    };
    
    //#endregion End of stuff for editing our held shares
    
    //#region Stuff for editing "stock lists"
    function addList(name) {
        (async () => {
            try {
                const res = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(name)}`, { method: 'POST' });
                if (res.status === 401 || res.status === 404) {
                    // Try an automatic dev login then retry once
                    const logged = await ensureLoggedInDev();
                    if (logged) {
                        const retry = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(name)}`, { method: 'POST' });
                        if (retry.ok) {
                            const created = await retry.json();
                            const id = created.stockListId || created.stock_list_id;
                            const newList = { stock_list_id: id, name: created.name, visibility: created.visibility || 'Private' };
                            setLocalStockLists(prev => [...prev, newList]);
                            setLocalStockHoldings(prev => ({ ...prev, [newList.stock_list_id]: [] }));
                            setSelectedListId(newList.stock_list_id);
                            setLocalVisibility(newList.visibility);
                            setHasChanges(true);
                            return;
                        }
                    }
                    window.alert('You must be logged in to create a stock list.');
                    return;
                }
                if (!res.ok) throw new Error('create failed');
                const created = await res.json();
                const id = created.stockListId || created.stock_list_id;

                const newList = { stock_list_id: id, name: created.name, visibility: created.visibility || 'Private' };
                setLocalStockLists(prev => [...prev, newList]);
                setLocalStockHoldings(prev => ({ ...prev, [newList.stock_list_id]: [] }));
                setSelectedListId(newList.stock_list_id);
                setLocalVisibility(newList.visibility);
                setHasChanges(true);
            } catch (err) {
                console.error('addList failed', err);
                const newList = { stock_list_id: `new_${Date.now()}`, name: name, visibility: 'Private' };
                setLocalStockLists(prev => [...prev, newList]);
                setLocalStockHoldings(prev => ({ ...prev, [newList.stock_list_id]: [] }));
                setSelectedListId(newList.stock_list_id);
                setLocalVisibility(newList.visibility);
                setHasChanges(true);
            }
        })();
    }
    
    async function removeList(listId) {
        try {
            const res = await fetch(`${backendUrl}/stocklists/${listId}`, { method: 'DELETE' });
            if (res.status === 401) { 
                const logged = await ensureLoggedInDev();
                if (logged) {
                    const retry = await fetch(`${backendUrl}/stocklists/${listId}`, { method: 'DELETE' });
                    if (retry.ok) {
                        const updatedLists = localStockLists.filter(list => list.stock_list_id !== listId);
                        setLocalStockLists(updatedLists);
                        setLocalStockHoldings(prev => { const next = { ...prev }; delete next[listId]; return next; });
                        if (updatedLists.length > 0) setSelectedListId(updatedLists[0].stock_list_id); else setSelectedListId(null);
                        return;
                    }
                }
                window.alert('You must be logged in to delete a stock list'); return; }
            if (res.status === 403) { window.alert('Access denied when deleting this stock list'); return; }
            if (!res.ok) throw new Error('delete failed');

            const updatedLists = localStockLists.filter(list => list.stock_list_id !== listId);
            setLocalStockLists(updatedLists);
            setLocalStockHoldings(prev => {
                const next = { ...prev };
                delete next[listId];
                return next;
            });

            if (updatedLists.length > 0) setSelectedListId(updatedLists[0].stock_list_id);
            else setSelectedListId(null);
        } catch (err) {
            console.error('removeList failed', err);
            window.alert('Failed to delete list');
        }
    }
    //#endregion

    //#region For reviews
    async function removeReview(reviewId) {
        try {
            const res = await fetch(`${backendUrl}/stocklists/reviews/${reviewId}`, { method: 'DELETE' });
            if (res.status === 401) { 
                const logged = await ensureLoggedInDev();
                if (logged) {
                    const retry = await fetch(`${backendUrl}/stocklists/reviews/${reviewId}`, { method: 'DELETE' });
                    if (retry.ok) {
                        setLocalReviews(prev => ({ ...prev, [selectedListId]: prev[selectedListId].filter(r => r.id !== reviewId) }));
                        setHasChanges(false);
                        return;
                    }
                }
                window.alert('You must be logged in to remove a review'); return; }
            if (res.status === 403) { window.alert('Access denied when deleting this review'); return; }
            if (!res.ok) throw new Error('delete failed');

            setLocalReviews(prev => ({
                ...prev,
                [selectedListId]: prev[selectedListId].filter(r => r.id !== reviewId)
            }));
            setHasChanges(false);
        } catch (err) {
            console.error('removeReview failed', err);
            // fallback to local-only removal if the server failed
            setLocalReviews(prev => ({
                ...prev,
                [selectedListId]: prev[selectedListId].filter(r => r.id !== reviewId)
            }));
            setHasChanges(true);
        }
    }
    //#endregion

    async function saveChanges() {
        // 🎯 Gotta make it so that the visibility of the portfolio takes the "newVisibility" value
        // and then I guess we need to pull the list data anew too? 🤔 Or just update it here locally.... idk!
        
        // First: persist any locally-created stock lists that use temporary ids (new_...)
        try {
            // snapshot local lists so we can modify safely
            for (const list of localStockLists) {
                if (typeof list.stock_list_id === 'string' && list.stock_list_id.startsWith('new_')) {
                    // create server-side
                    try {
                        const res = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(list.name)}`, { method: 'POST' });
                        if (res.status === 401) {
                            const logged = await ensureLoggedInDev();
                            if (logged) {
                                const retry = await fetch(`${backendUrl}/stocklists?name=${encodeURIComponent(list.name)}`, { method: 'POST' });
                                if (retry.ok) {
                                    var created = await retry.json();
                                } else { continue; }
                            } else continue;
                        } else if (!res.ok) {
                            continue;
                        } else {
                            var created = await res.json();
                        }

                        const newId = created.stockListId || created.stock_list_id;
                        // replace id in localStockLists
                        setLocalStockLists(prev => prev.map(p => p.stock_list_id === list.stock_list_id ? { ...p, stock_list_id: newId } : p));

                        // move any holdings keyed by old temp id to new id
                        setLocalStockHoldings(prev => {
                            if (!prev) return prev;
                            const next = { ...prev };
                            if (next[list.stock_list_id]) {
                                next[newId] = next[list.stock_list_id];
                                delete next[list.stock_list_id];
                            }
                            return next;
                        });

                        // update selectedListId if it was the temp id
                        if (selectedListId === list.stock_list_id) {
                            setSelectedListId(newId);
                        }
                    } catch (err) {
                        console.error('failed to persist temp list', list, err);
                    }
                }
            }
        } catch (err) {
            console.error('error while persisting new lists', err);
        }

        // Next: persist any new holdings that were added locally and still have a temp id
        try {
            for (const key of Object.keys(localStockHoldings || {})) {
                const holdingsForList = localStockHoldings[key] || [];
                for (const h of holdingsForList) {
                    if (typeof h.holding_id === 'string' && h.holding_id.startsWith('new_')) {
                        try {
                            // ensure key is server id (if key still a temp id, we attempted to persist list above and should have swapped keys)
                            const actualListId = key;
                            const res = await fetch(`${backendUrl}/stocklists/${actualListId}/holdings?symbol=${encodeURIComponent(h.symbol)}&shares=${encodeURIComponent(h.shares)}`, { method: 'POST' });
                            if (res.status === 401) {
                                const logged = await ensureLoggedInDev();
                                if (logged) {
                                    const retry = await fetch(`${backendUrl}/stocklists/${actualListId}/holdings?symbol=${encodeURIComponent(h.symbol)}&shares=${encodeURIComponent(h.shares)}`, { method: 'POST' });
                                    if (retry.ok) {
                                        const created = await retry.json();
                                        // replace temp id with server id
                                        setLocalStockHoldings(prev => ({ ...prev, [actualListId]: prev[actualListId].map(x => x.holding_id === h.holding_id ? { ...x, holding_id: created.holdingId || created.holding_id } : x) }));
                                    }
                                }
                            } else if (res.ok) {
                                const created = await res.json();
                                setLocalStockHoldings(prev => ({ ...prev, [actualListId]: prev[actualListId].map(x => x.holding_id === h.holding_id ? { ...x, holding_id: created.holdingId || created.holding_id } : x) }));
                            }
                        } catch (err) {
                            console.error('failed to persist temp holding', h, err);
                        }
                    }
                }
            }
        } catch (err) {
            console.error('error while persisting new holdings', err);
        }

        // persist visibility change to backend for selected list (if server id)
        try {
            // selectedListId may be a temp id (string) -> skip
            if (selectedListId && !(typeof selectedListId === 'string' && selectedListId.startsWith('new_'))) {
                const visRes = await fetch(`${backendUrl}/stocklists/${selectedListId}/visibility?visibility=${encodeURIComponent(localVisibility)}`, { method: 'PUT' });
                if (visRes.status === 401) {
                    const logged = await ensureLoggedInDev();
                    if (logged) {
                        await fetch(`${backendUrl}/stocklists/${selectedListId}/visibility?visibility=${encodeURIComponent(localVisibility)}`, { method: 'PUT' });
                    }
                }
            }
        } catch (err) {
            console.error('failed to save visibility', err);
        }

        // For now, update local state to simulate DB sync for visibility
        setLocalStockLists(localStockLists.map(list =>
            list.stock_list_id === selectedListId
                ? { ...list, visibility: localVisibility }
                : list
        ));
        
        // We'll also simulate the mock data for the stock holdings too
        mockStockHoldings[selectedListId] = localStockHoldings[selectedListId];

        // And the same for the stock lists!
        mockStockList.length = 0;
        mockStockList.push(...localStockLists);

        // Same for reviews
        mockReviews[selectedListId] = localReviews[selectedListId];

        setHasChanges(false);
    }

    function discardChanges() {
        const originalVisibility = localStockLists.find(l => l.stock_list_id === selectedListId)?.visibility;
        setLocalVisibility(originalVisibility || '');
        setLocalStockHoldings(mockStockHoldings);  // or re-fetch from DB
        setLocalStockLists(mockStockList);  // same as abv
        setLocalReviews(mockReviews);  // and again

        setHasChanges(false);
    }

    const [createStockListModalVisible, setCreateStockListModalVisible] = useState(false);

    return <div style={{ display: 'flex', height: '100vh', overflowY: 'clip'}}>
        <CreateStockListModal 
            isOpen={createStockListModalVisible}
            onClose={() => {
                setCreateStockListModalVisible(false);
            }}
            existingListNames={(newName) => localStockLists.find((item) => item.name === newName)}
            onConfirm={(newName) => {
                addList(newName)
                setCreateStockListModalVisible(false);
            }}
        />
        
        {/* --- LEFT SIDE: Stock Lists --- */}
        <div style={{ width: '25%', height: '100vh', borderRight: '1px solid #ccc', padding: '20px', backgroundColor: '#f9f9f9' }}>
            
            <Title text={'My Stock Lists'}/>
            
            {/* List of Stock Lists */}
            <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {localStockLists.map(
                    item => (<button 
                        key={item.stock_list_id} 
                        style={BigButtonStyle(selectedListId === item.stock_list_id)}
                        onClick={() => selectList(item.stock_list_id)}
                    >
                        {item.name}
                    </button>)
                )}
                {localStockLists.length === 0 && <EmptyListText text="You have no lists"/> }
               
            </div>

            {/* Add/Remove Controls */}
            <div style={{ marginTop: '20px', display: 'flex', gap: '10px' }}>
                <button 
                    onClick={() => {
                        if (hasChanges) {
                            window.alert('You have unsaved changes. Save or discard them first');
                            return;
                        }
                        setCreateStockListModalVisible(true)
                    }}
                >
                    Add Stock List
                </button>
                <button
                    onClick={() => removeList(selectedListId)}
                > 
                    Remove Selected 
                </button>
            </div>
        </div>

        {/* --- RIGHT SIDE: Performance & Details --- */}
        <RightSide 
            stockList = {localStockLists.find((list => list.stock_list_id === selectedListId))}
            stockHoldings = {localStockHoldings[selectedListId] || []}
            localVisibility={localVisibility}
            reviews={localReviews[selectedListId] || []}
            updateLocalVisibility = {updateLocalVisibility}
            removeStockHolding={removeStockHolding}
            updateStockHolding={updateStockHolding}
            addStockHolding={addStockHolding}
            hasStockHolding={hasStockHolding}
            removeReview={removeReview}
            hasChanges={hasChanges}
            saveChanges={saveChanges}
            discardChanges={discardChanges}
        />
    </div> 
}

function RightSide({stockList, stockHoldings, localVisibility, reviews, updateLocalVisibility, updateStockHolding, removeStockHolding, addStockHolding, hasStockHolding, removeReview, hasChanges, saveChanges, discardChanges})
{
    const [showStockPerformance, setShowStockPerformance] = useState(false);
    const [stockSymbolViewing, setStockSymbolViewing] = useState('');

    const [addStockModalVisible, setAddStockModalVisible] = useState(false);
    
    if (!stockList) {
        return (
            <div style={{ display: 'flex', width: '75%', padding: '20px', alignItems: 'center', justifyContent: 'center' }}>
                <EmptyListText text={'Pick a stock list'}/>
            </div>
        );
    }
    return <>
        <AddNewStockModal 
            isOpen={addStockModalVisible}
            onClose={() => setAddStockModalVisible(false)}
            hasStockHolding={hasStockHolding}
            onConfirm={(newStockHolding) => {
                // 🎯 Make the changes (i.e., adding the stock) STICK!
                // Means we send the new data to the DB when we add a new stock
                console.log('hihiihi');
                setAddStockModalVisible(false);

                addStockHolding(newStockHolding.stockSymbol, newStockHolding.stockAmount);
            }}
        />
        
        <StockPerformance 
            symbol={stockSymbolViewing}
            isOpen={showStockPerformance} 
            onClose={() => setShowStockPerformance(false)} 
        />

        <div style={{ width: '75%', padding: '20px', overflowY: 'scroll' }}>

            {/* Header div */}
            <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}} >
                <h2>{stockList.name}</h2>
                {hasChanges && (
                    <div style={{ display: 'flex', gap: '10px' }}>
                        💾
                        <button onClick={discardChanges}>Discard</button>
                        <button onClick={saveChanges} style={{
                            backgroundColor: '#4CAF50',
                            color: 'white',
                            border: '1px solid var(--border-color)',
                            borderRadius: '5px'
                        }}>Save</button>
                    </div>
                )}
            </div>
            
            {/* Content! */}
            
            <div style={{ display: 'flex', gap: '10px', marginBottom: '20px' }}>
                {['Private', 'Public', 'Friends'].map(visibility => (
                    <button
                        key={visibility}
                        style={{
                            padding: '2px 4px',
                            backgroundColor:  localVisibility === visibility ? 'var(--select-background)' : '#f0f0f0',
                            border: '1px solid #ddd',
                            cursor: 'pointer',
                            borderRadius: '4px'
                        }}
                        onClick={() => updateLocalVisibility(visibility)}
                    >
                        {visibility}
                    </button>
                ))}
            </div>

            <h2>📈 List Covariance: </h2>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '15px' }}>
                {stockHoldings.map(stock => (
                    <div key={stock.symbol} style={{ border: '1px solid #ddd', padding: '15px', borderRadius: '4px' }}>
                        <div style={{display: 'flex', justifyContent: 'space-between', alignItems: 'center'}}> 
                            <h3>{stock.name} {stock.symbol}</h3> 
                            <button onClick={() => removeStockHolding(stock.holding_id)}>🗑️</button>
                        </div>
                        <p><strong>Number of shares:</strong>  <input
                            className='text-field-input'
                            type="number"
                            value={stock.shares}
                            min="1"

                            onChange={(e) => updateStockHolding(stock.holding_id, parseInt(e.target.value) || 0)}
                        /></p> 

                        <button onClick={() => {setStockSymbolViewing(stock.symbol); setShowStockPerformance(true);}}>View {stock.symbol} Performance</button>
                    </div>
                ))}
                {stockHoldings.length === 0 && <EmptyListText text="No Stock Holdings"/> }
                
                <button 
                    onClick={() => setAddStockModalVisible(true) } 
                    style={{ marginTop: '10px', padding: '10px', border: 'none', borderRadius: '4px', cursor: 'pointer' }}
                >
                    Add New Stock
                </button>

            </div>
            <Reviews 
                reviews={reviews}
                removeReview={removeReview}
            />

            <div style={{ height: '20vh' }} />
        </div>
    </>
}

function Reviews({ reviews, removeReview }) {
    return (
        <div style={{ marginTop: '30px', borderTop: '1px solid #ddd', paddingTop: '20px' }}>
            <h3>Reviews</h3>
            <div style={{ maxHeight: '300px', overflowY: 'auto', display: 'flex', flexDirection: 'column', gap: '10px' }}>
                {reviews.map(review => (
                    <div key={review.id} style={{ border: '1px solid #eee', padding: '10px', borderRadius: '4px', backgroundColor: '#fafafa', display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                        <div>
                            <p style={{ margin: '0 0 5px 0', fontSize: '0.9em', color: '#666' }}>{review.email}</p>
                            <p style={{ margin: '0', fontSize: '0.95em' }}>{review.text}</p>
                        </div>
                        <button onClick={() => removeReview(review.id)}>🗑️</button>
                    </div>
                ))}
                {reviews.length === 0 && <EmptyListText text={'No Reviews Yet'}/> }
            </div>
        </div>
    );
}

function CreateStockListModal({ isOpen, onClose, existingListNames, onConfirm }) {
    const [listName, setListName] = useState('');
    const [canConfirm, setCanConfirm] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        if (isOpen) {
            setListName('');
            setError('');
        }
    }, [isOpen]);

    useEffect(() => {
        if (!listName.trim()) {
            setCanConfirm(false);
            return;
        }
        if (listName.length >= 255) {
            setError('The name is too long!')
            setCanConfirm(false);
            return;
        }
        if (existingListNames(listName)) {
            setError('A list with this name already exists');
            setCanConfirm(false);
            return;
        }
        setError('')
        setCanConfirm(true);
    }, [listName])

    if (!isOpen) return null;

    return (
        <div style={{ position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.5)', display: 'flex', alignItems: 'center', justifyContent: 'center', zIndex: 1000 }}>
            <div style={{ backgroundColor: 'white', padding: '30px', borderRadius: '8px', boxShadow: '0 2px 10px rgba(0,0,0,0.2)', minWidth: '300px' }}>
                <h2>Create New Stock List</h2>
                
                {error && <p style={{ color: '#d32f2f', marginBottom: '10px' }}>{error}</p>}
                
                <input 
                    type="text" 
                    placeholder="Enter list name" 
                    value={listName} 
                    onChange={(e) => setListName(e.target.value)}
                    className='regular-input'
                />

                <div style={{ display: 'flex', gap: '10px', justifyContent: 'flex-end' }}>
                    <button onClick={onClose}>Cancel</button>
                    <button 
                        disabled={!canConfirm} 
                        onClick={() => onConfirm( listName )}
                    >
                        Confirm
                    </button>
                </div>
            </div>
        </div>
    );
}
