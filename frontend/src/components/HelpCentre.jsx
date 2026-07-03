import { useState } from 'react';
import api from '../services/api';

export default function HelpCentre({ isOpen, onClose }) {
  const [email, setEmail] = useState('');
  const [query, setQuery] = useState('');
  const [status, setStatus] = useState({ type: '', message: '' });
  const [loading, setLoading] = useState(false);

  if (!isOpen) return null;

  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);
    setStatus({ type: '', message: '' });
    try {
      const res = await api.post('/help/query', { email, query });
      setStatus({ type: 'success', message: res.data?.message || 'Query submitted successfully. We will get back to you soon.' });
      setEmail('');
      setQuery('');
      setTimeout(() => {
        onClose();
        setStatus({ type: '', message: '' });
      }, 1500);
    } catch (error) {
      setStatus({ type: 'error', message: error.response?.data?.message || 'Failed to submit query. Please try again.' });
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/70 backdrop-blur-sm p-4 animate-fade-in">
      <div className="glass-card relative w-full max-w-md rounded-2xl p-8 border border-stone-700 animate-slide-up shadow-2xl">
        <button 
          onClick={onClose}
          className="absolute right-4 top-4 text-stone-400 hover:text-white transition-colors"
        >
          <svg className="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor" strokeWidth={2}>
            <path strokeLinecap="round" strokeLinejoin="round" d="M6 18L18 6M6 6l12 12" />
          </svg>
        </button>
        <div className="text-center mb-8">
          <h2 className="text-3xl font-extrabold text-stone-100 tracking-tight font-[Montserrat]">Help Centre</h2>
          <p className="mt-2 text-sm text-stone-400">Have a question? Send us a query and we'll get back to you.</p>
        </div>
        
        {status.message && (
          <div className={`p-4 rounded-xl mb-6 shadow-sm flex items-start gap-3 border ${status.type === 'success' ? 'bg-emerald-900/20 border-emerald-900/50 text-emerald-400' : 'bg-rose-900/20 border-rose-900/50 text-rose-400'}`}>
             <p className="font-medium text-sm">{status.message}</p>
          </div>
        )}
        <form onSubmit={handleSubmit} className="space-y-5">
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Email Address</label>
            <input type="email" required value={email} onChange={(e) => setEmail(e.target.value)} className="form-input" placeholder="your@email.com" />
          </div>
          <div>
            <label className="block text-sm font-semibold text-stone-300 mb-2">Your Query</label>
            <textarea required rows="4" value={query} onChange={(e) => setQuery(e.target.value)} className="form-input resize-none" placeholder="How can we help you?"></textarea>
          </div>
          <button type="submit" disabled={loading} className="w-full btn-glow py-3 text-lg mt-2 disabled:opacity-50">
            {loading ? 'Submitting...' : 'Submit Query'}
          </button>
        </form>
      </div>
    </div>
  );
}
