import React, { useState, useEffect } from 'react';
import { useParams, useNavigate, useLocation } from 'react-router-dom';
import api from '../services/api';
import useAuth from '../hooks/useAuth';
import { Clock, CreditCard, ShieldCheck } from 'lucide-react';

export default function Checkout() {
    const { id: eventId } = useParams();
    const navigate = useNavigate();
    const location = useLocation();
    const { user } = useAuth();
    
    const [timeLeft, setTimeLeft] = useState(0);
    const [booking, setBooking] = useState(false);
    const [error, setError] = useState('');
    
    const selectedSeats = location.state?.selectedSeats || [];
    const holdExpiresAt = location.state?.holdExpiresAt;

    useEffect(() => {
        if (!selectedSeats.length || !holdExpiresAt) {
            navigate(`/events/${eventId}/seats`);
            return;
        }

        const expiryTime = new Date(holdExpiresAt).getTime();
        
        const updateTimer = () => {
            const now = new Date().getTime();
            const difference = expiryTime - now;
            
            if (difference <= 0) {
                clearInterval(interval);
                setTimeLeft(0);
                alert("Your seat hold has expired. Please select seats again.");
                navigate(`/events/${eventId}/seats`);
            } else {
                setTimeLeft(Math.floor(difference / 1000));
            }
        };

        updateTimer();
        const interval = setInterval(updateTimer, 1000);

        return () => clearInterval(interval);
    }, [holdExpiresAt, navigate, eventId, selectedSeats]);

    const formatTime = (seconds) => {
        const m = Math.floor(seconds / 60);
        const s = seconds % 60;
        return `${m}:${s < 10 ? '0' : ''}${s}`;
    };

    const handleCheckout = async () => {
        setBooking(true);
        setError('');
        
        try {
            const seatIds = selectedSeats.map(s => s.id);
            const response = await api.post('/bookings', {
                eventId: Number(eventId),
                seatIds: seatIds
            });
            
            navigate(`/booking-success/${response.data.data.bookingReference}`, {
                state: { bookingData: response.data.data }
            });
        } catch (err) {
            setError(err.response?.data?.message || 'Failed to process booking.');
            setBooking(false);
        }
    };

    const handleCancel = async () => {
        try {
            const seatIds = selectedSeats.map(s => s.id);
            await api.post('/seats/release', {
                eventId: Number(eventId),
                seatIds: seatIds
            });
        } catch (e) {
            console.error(e);
        } finally {
            navigate(`/events/${eventId}/seats`);
        }
    };

    if (!selectedSeats.length) return null;

    const totalAmount = selectedSeats.reduce((sum, seat) => sum + seat.price, 0);

    return (
        <div className="max-w-4xl mx-auto space-y-8 animate-fade-in pb-20">
            <div className="flex justify-between items-center bg-orange-900/20 border border-orange-500/30 p-4 rounded-xl shadow-inner">
                <div className="flex items-center gap-3">
                    <Clock className="text-orange-500 h-6 w-6" />
                    <div>
                        <h3 className="font-semibold text-orange-400">Seats Held</h3>
                        <p className="text-sm text-orange-300/80">Please complete your purchase before the timer runs out.</p>
                    </div>
                </div>
                <div className="text-2xl font-bold text-orange-500 font-mono tracking-wider bg-stone-900/80 px-4 py-2 rounded-lg shadow-sm border border-orange-500/20">
                    {formatTime(timeLeft)}
                </div>
            </div>

            {error && (
                <div className="bg-rose-900/20 border border-rose-900/50 rounded-xl p-4 flex items-center shadow-sm text-rose-400 font-medium">
                    {error}
                </div>
            )}

            <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
                {/* Order Summary */}
                <div className="glass-card rounded-2xl p-6">
                    <h2 className="text-xl font-bold text-stone-100 mb-6 border-b border-stone-700 pb-4 font-[Montserrat]">Order Summary</h2>
                    
                    <div className="space-y-4">
                        {selectedSeats.map(seat => (
                            <div key={seat.id} className="flex justify-between items-center py-2 border-b border-stone-800 last:border-0">
                                <div>
                                    <p className="font-medium text-stone-200">Seat {seat.seatLabel}</p>
                                    <p className="text-xs text-stone-400 capitalize">{seat.category.toLowerCase()}</p>
                                </div>
                                <span className="font-medium text-stone-200">${seat.price.toFixed(2)}</span>
                            </div>
                        ))}
                    </div>

                    <div className="mt-6 pt-6 border-t border-stone-700 flex justify-between items-center">
                        <span className="text-lg font-bold text-stone-100">Total Amount</span>
                        <span className="text-2xl font-bold text-amber-500">${totalAmount.toFixed(2)}</span>
                    </div>
                </div>

                {/* Payment Details (Mock) */}
                <div className="glass-card rounded-2xl p-6 h-fit">
                    <h2 className="text-xl font-bold text-stone-100 mb-6 border-b border-stone-700 pb-4 font-[Montserrat]">Payment Information</h2>
                    
                    <div className="space-y-4">
                        <div className="bg-stone-900/50 p-4 rounded-xl border border-stone-700 flex gap-4 items-start">
                            <ShieldCheck className="text-amber-500 w-6 h-6 mt-0.5 shrink-0" />
                            <div>
                                <h4 className="font-medium text-stone-200">Secure Checkout</h4>
                                <p className="text-sm text-stone-400 mt-1">This is a mock checkout. No real payment will be processed. Clicking "Confirm Booking" will finalize your seats and send an email with your QR ticket.</p>
                            </div>
                        </div>

                        <div className="pt-4 space-y-3">
                            <button
                                onClick={handleCheckout}
                                disabled={booking}
                                className="w-full flex items-center justify-center gap-2 btn-glow py-3 disabled:opacity-70 disabled:cursor-not-allowed"
                            >
                                {booking ? (
                                    <div className="animate-spin h-5 w-5 border-2 border-stone-900 border-t-transparent rounded-full"></div>
                                ) : (
                                    <CreditCard className="w-5 h-5" />
                                )}
                                {booking ? 'Processing...' : `Pay $${totalAmount.toFixed(2)} & Confirm`}
                            </button>
                            
                            <button
                                onClick={handleCancel}
                                disabled={booking}
                                className="w-full py-3 text-stone-400 hover:text-rose-400 font-medium transition-colors"
                            >
                                Cancel Order
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
}
