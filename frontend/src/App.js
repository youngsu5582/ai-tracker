import React from 'react';
import { BrowserRouter as Router, Routes, Route, Link } from 'react-router-dom';
import { FaHistory, FaTachometerAlt, FaSearch } from 'react-icons/fa'; // Import icons
import './App.css';
import History from './pages/History';
import Dashboard from '././pages/Dashboard';
import PromptList from './pages/PromptList';
import Search from './pages/Search';

function App() {
    return (
        <Router>
            <div className="App">
                <header className="app-header">
                    <div className="app-title-container">
                        <img src="/logo.png" alt="AI Activity Tracker Logo" className="app-logo" />
                        <h1>AI Activity Tracker</h1>
                    </div>
                    <nav>
                        <Link to="/"><FaHistory /> History</Link>
                        <Link to="/search"><FaSearch /> Search</Link>
                        <Link to="/dashboard"><FaTachometerAlt /> Dashboard</Link>
                    </nav>
                </header>
                <main>
                    <Routes>
                        <Route path="/" element={<History />} />
                        <Route path="/search" element={<Search />} />
                        <Route path="/dashboard" element={<Dashboard />} />
                        <Route path="/prompts/:category" element={<PromptList />} />
                    </Routes>
                </main>
            </div>
        </Router>
    );
}

export default App;
