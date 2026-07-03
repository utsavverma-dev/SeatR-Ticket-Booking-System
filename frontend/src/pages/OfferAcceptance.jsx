import { useState, useEffect } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { waitlistService } from '../services/waitlistService';
import useAuth from '../hooks/useAuth';

const OfferAcceptance = () => {
  const { token } = useParams();
  const navigate = useNavigate();
  const { user } = useAuth();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  const [bookingRef, setBookingRef] = useState('');
  
  // Timer for display
  const [timeLeft, setTimeLeft] = useState(600); // 10 mins in seconds

  useEffect(() => {
    if (!user) {
      navigate('/login', { state: { from: { pathname: `/offers/${token}` } } });
      return;
    }

    const timer = setInterval(() => {
      setTimeLeft((prev) => {
        if (prev <= 1) {
          clearInterval(timer);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(timer);
  }, [user, navigate, token]);

  const handleAcceptOffer = async () => {
    setLoading(true);
    setError('');
    try {
      const response = await waitlistService.acceptOffer(token);
      setSuccess(true);
      setBookingRef(response.data.bookingReference);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to accept offer. It may have expired.');
    } finally {
      setLoading(false);
    }
  };

  const minutes = Math.floor(timeLeft / 60);
  const seconds = timeLeft % 60;

  if (success) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center animate-fade-in">
        <div className="glass-card rounded-3xl p-12 border border-stone-800 relative overflow-hidden">
          <div className="absolute top-0 left-0 right-0 h-2 bg-gradient-to-r from-emerald-600 to-emerald-400"></div>
          <div className="w-20 h-20 bg-emerald-900/30 rounded-full flex items-center justify-center mx-auto mb-6 shadow-[0_0_30px_rgba(16,185,129,0.2)]">
            <svg className="w-10 h-10 text-emerald-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 13l4 4L19 7" />
            </svg>
          </div>
          <h2 className="text-3xl font-bold text-stone-100 mb-4 font-[Montserrat]">Offer Accepted!</h2>
          <p className="text-lg text-stone-300 mb-8">Your booking has been successfully created.</p>
          <div className="bg-stone-900/50 border border-stone-700 rounded-xl p-6 mb-8 inline-block min-w-[300px]">
            <p className="text-sm text-stone-400 mb-1">Booking Reference</p>
            <p className="text-2xl font-mono font-bold text-amber-500 glow-accent-text">{bookingRef}</p>
          </div>
          <button
            onClick={() => navigate('/history')}
            className="block w-full sm:w-auto mx-auto btn-glow py-3 px-8"
          >
            View Booking Details
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="max-w-3xl mx-auto px-4 py-16 animate-fade-in pb-20">
      <div className="glass-card rounded-3xl border border-stone-800 overflow-hidden">
        <div className="bg-amber-900/20 border-b border-amber-900/30 p-8 text-center">
          <h1 className="text-3xl font-bold mb-2 text-stone-100 font-[Montserrat]">Waitlist Offer</h1>
          <p className="text-amber-500/80">You have a limited time to accept this offer</p>
        </div>
        
        <div className="p-8 text-center">
          <div className="mb-8">
            <p className="text-sm text-stone-500 uppercase tracking-wider font-semibold mb-2">Time Remaining</p>
            <div className={`text-5xl font-mono font-bold ${timeLeft < 60 ? 'text-rose-500 animate-pulse' : 'text-stone-100'}`}>
              {minutes.toString().padStart(2, '0')}:{seconds.toString().padStart(2, '0')}
            </div>
            {timeLeft === 0 && (
              <p className="text-rose-500 mt-4 font-medium">This offer has expired.</p>
            )}
          </div>

          {error && (
            <div className="bg-rose-900/20 text-rose-400 p-4 rounded-xl mb-8 text-left border border-rose-900/50 shadow-sm">
              <div className="flex items-start">
                <svg className="w-5 h-5 mr-3 mt-0.5 flex-shrink-0" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
                </svg>
                <p>{error}</p>
              </div>
            </div>
          )}

          <div className="flex flex-col sm:flex-row gap-4 justify-center">
            <button
              onClick={handleAcceptOffer}
              disabled={loading || timeLeft === 0}
              className="btn-glow py-4 px-8 flex-1 max-w-[250px] disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {loading ? 'Processing...' : 'Accept & Book Now'}
            </button>
            <button
              onClick={() => navigate('/')}
              className="btn-ghost border border-stone-700 hover:border-stone-600 font-bold py-4 px-8 rounded-xl flex-1 max-w-[250px]"
            >
              Decline Offer
            </button>
          </div>
          
          <p className="text-xs text-stone-500 mt-8">
            By accepting, you agree to the standard terms and conditions of ticketing.
          </p>
        </div>
      </div>
    </div>
  );
};

export default OfferAcceptance;
