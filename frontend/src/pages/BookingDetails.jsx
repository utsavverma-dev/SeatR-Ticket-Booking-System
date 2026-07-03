import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import api from '../services/api';
import { Calendar, MapPin, Ticket, CreditCard, ArrowLeft, Trash2, AlertTriangle, ShieldCheck } from 'lucide-react';

export default function BookingDetails() {
    const { id } = useParams();
    const navigate = useNavigate();
    
    const [booking, setBooking] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState('');
    const [cancelling, setCancelling] = useState(false);

    useEffect(() => {
        fetchBookingDetails();
    }, [id]);

    const fetchBookingDetails = async () => {
        try {
            const response = await api.get(`/bookings/${id}`);
            setBooking(response.data.data);
        } catch (err) {
            setError('Failed to load booking details.');
        } finally {
            setLoading(false);
        }
    };

    const handleCancelBooking = async () => {
        if (!window.confirm("Are you sure you want to cancel this booking? This action cannot be undone.")) {
            return;
        }
        
        setCancelling(true);
        try {
            await api.delete(`/bookings/${id}`);
            alert("Booking cancelled successfully.");
            fetchBookingDetails(); // Refresh details to show cancelled status
        } catch (err) {
            alert(err.response?.data?.message || "Failed to cancel booking.");
        } finally {
            setCancelling(false);
        }
    };

    if (loading) {
        return (
            <div className="flex justify-center items-center h-64">
                <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-amber-500"></div>
            </div>
        );
    }

    if (error || !booking) {
        return (
            <div className="text-center mt-10 bg-rose-900/20 p-8 rounded-2xl max-w-lg mx-auto border border-rose-900/50">
                <p className="text-rose-400 font-medium mb-4">{error || "Booking not found"}</p>
                <Link to="/bookings" className="text-amber-500 hover:text-amber-400 font-semibold inline-block">Back to My Bookings</Link>
            </div>
        );
    }

    const eventDate = new Date(booking.eventDate);
    const isPastEvent = eventDate < new Date();
    const canCancel = booking.status === 'CONFIRMED' && !isPastEvent;

    return (
        <div className="max-w-4xl mx-auto space-y-8 animate-fade-in pb-12">
            <Link to="/bookings" className="inline-flex items-center text-stone-400 hover:text-amber-500 font-medium transition-colors">
                <ArrowLeft className="w-4 h-4 mr-2" /> Back to My Bookings
            </Link>

            <div className="glass-card rounded-2xl overflow-hidden border border-stone-800">
                {/* Header */}
                <div className={`p-6 sm:p-8 ${booking.status === 'CONFIRMED' ? 'bg-amber-900/20 border-b border-amber-900/30' : 'bg-stone-800/80 border-b border-stone-700'}`}>
                    <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
                        <div>
                            <span className={`inline-block px-3 py-1 rounded-full text-xs font-bold uppercase tracking-wider mb-4 border ${
                                booking.status === 'CONFIRMED' ? 'bg-emerald-900/40 text-emerald-400 border-emerald-800' : 'bg-stone-700 text-stone-300 border-stone-600'
                            }`}>
                                {booking.status}
                            </span>
                            <h1 className="text-3xl font-bold text-stone-100 font-[Montserrat]">{booking.eventTitle}</h1>
                        </div>
                        <div className="text-left sm:text-right">
                            <p className="text-stone-400 text-sm uppercase tracking-wider mb-1">Booking Ref</p>
                            <p className="text-xl font-mono font-bold text-amber-500 glow-accent-text">{booking.bookingReference}</p>
                        </div>
                    </div>
                </div>

                {/* Content */}
                <div className="p-6 sm:p-8 space-y-8">
                    {/* Event & Venue Info */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div className="flex items-start gap-4">
                            <div className="w-12 h-12 rounded-full bg-stone-900/50 border border-stone-700 flex items-center justify-center shrink-0">
                                <Calendar className="w-6 h-6 text-amber-500" />
                            </div>
                            <div>
                                <h3 className="font-semibold text-stone-100 mb-1 font-[Montserrat]">Date & Time</h3>
                                <p className="text-stone-300">{eventDate.toLocaleDateString('en-US', { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}</p>
                                <p className="text-stone-300">{eventDate.toLocaleTimeString('en-US', { hour: '2-digit', minute: '2-digit' })}</p>
                            </div>
                        </div>
                        <div className="flex items-start gap-4">
                            <div className="w-12 h-12 rounded-full bg-stone-900/50 border border-stone-700 flex items-center justify-center shrink-0">
                                <MapPin className="w-6 h-6 text-amber-500" />
                            </div>
                            <div>
                                <h3 className="font-semibold text-stone-100 mb-1 font-[Montserrat]">Venue</h3>
                                <p className="text-stone-300">{booking.venueName}</p>
                            </div>
                        </div>
                    </div>

                    <hr className="border-stone-800" />

                    {/* Customer & Payment Info */}
                    <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                        <div>
                            <h3 className="text-lg font-bold text-stone-100 mb-4 font-[Montserrat]">Customer Details</h3>
                            <p className="text-stone-400"><span className="font-medium text-stone-200">Name:</span> {booking.customerName}</p>
                            <p className="text-stone-400"><span className="font-medium text-stone-200">Email:</span> {booking.customerEmail}</p>
                            <p className="text-stone-400"><span className="font-medium text-stone-200">Booked On:</span> {new Date(booking.bookingTime).toLocaleString()}</p>
                        </div>
                        <div className="bg-stone-900/50 p-6 rounded-xl border border-stone-700">
                            <h3 className="text-lg font-bold text-stone-100 mb-4 flex items-center gap-2 font-[Montserrat]">
                                <ShieldCheck className="w-5 h-5 text-emerald-500" /> Payment Summary
                            </h3>
                            <div className="flex justify-between items-center mb-2">
                                <span className="text-stone-400">Total Seats ({booking.seats.length})</span>
                            </div>
                            <div className="flex justify-between items-center border-t border-stone-700 mt-4 pt-4">
                                <span className="font-bold text-stone-100">Total Paid</span>
                                <span className="text-2xl font-bold text-amber-500">${booking.totalAmount.toFixed(2)}</span>
                            </div>
                        </div>
                    </div>

                    <hr className="border-stone-800" />

                    {/* Seat Details */}
                    <div>
                        <h3 className="text-lg font-bold text-stone-100 mb-4 flex items-center gap-2 font-[Montserrat]">
                            <Ticket className="w-5 h-5 text-amber-500" /> Seat Details
                        </h3>
                        <div className="bg-stone-900/30 border border-stone-800 rounded-xl overflow-hidden">
                            <table className="min-w-full divide-y divide-stone-800">
                                <thead className="bg-stone-900/80">
                                    <tr>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-stone-400 uppercase tracking-wider">Seat Number</th>
                                        <th className="px-6 py-3 text-left text-xs font-medium text-stone-400 uppercase tracking-wider">Category</th>
                                        <th className="px-6 py-3 text-right text-xs font-medium text-stone-400 uppercase tracking-wider">Price</th>
                                    </tr>
                                </thead>
                                <tbody className="divide-y divide-stone-800">
                                    {booking.seats.map((seat, idx) => (
                                        <tr key={idx}>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm font-medium text-stone-200">{seat.seatLabel}</td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-stone-400 capitalize">{seat.category.toLowerCase()}</td>
                                            <td className="px-6 py-4 whitespace-nowrap text-sm text-stone-200 text-right">${seat.price.toFixed(2)}</td>
                                        </tr>
                                    ))}
                                </tbody>
                            </table>
                        </div>
                    </div>

                    {/* Cancellation Action */}
                    {canCancel && (
                        <div className="mt-8 pt-8 border-t border-rose-900/30">
                            <div className="bg-rose-900/10 border border-rose-900/50 rounded-xl p-6 flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 shadow-sm">
                                <div>
                                    <h4 className="text-rose-400 font-bold flex items-center gap-2 mb-1">
                                        <AlertTriangle className="w-5 h-5" /> Cancel Booking
                                    </h4>
                                    <p className="text-stone-400 text-sm">Need to change your plans? You can cancel your booking and release these seats.</p>
                                </div>
                                <button
                                    onClick={handleCancelBooking}
                                    disabled={cancelling}
                                    className="shrink-0 px-6 py-2.5 bg-rose-700 hover:bg-rose-600 text-white rounded-lg font-medium transition-colors flex items-center gap-2 disabled:opacity-70 disabled:cursor-not-allowed shadow-[0_0_15px_rgba(225,29,72,0.3)] hover:shadow-[0_0_20px_rgba(225,29,72,0.5)]"
                                >
                                    {cancelling ? 'Cancelling...' : <><Trash2 className="w-4 h-4" /> Cancel Tickets</>}
                                </button>
                            </div>
                        </div>
                    )}
                </div>
            </div>
        </div>
    );
}
