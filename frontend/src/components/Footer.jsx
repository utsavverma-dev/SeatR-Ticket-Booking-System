import { useState } from 'react';
import HelpCentre from './HelpCentre';

const Footer = () => {
  const [isHelpOpen, setIsHelpOpen] = useState(false);
  return (
    <footer className="bg-stone-900 border-t border-amber-900/30 text-stone-400 py-12">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 flex flex-col md:flex-row justify-between items-start md:items-center">
        <div className="mb-8 md:mb-0">
          <span className="text-2xl font-extrabold text-stone-100 tracking-tight flex items-center gap-2">
            <div className="flex h-8 w-8 items-center justify-center rounded-lg bg-gradient-to-br from-amber-500 to-amber-700 shadow-lg shadow-amber-500/20">
              <span className="text-stone-900 font-bold text-sm">B</span>
            </div>
            BookIT
          </span>
          <p className="mt-3 text-sm max-w-sm text-stone-500">The premium platform for immersive events and VIP concert experiences. Designed for those who value elegance.</p>
        </div>
        <div className="flex flex-col sm:flex-row space-y-4 sm:space-y-0 sm:space-x-8 text-sm font-medium">
          <a href="#" className="hover:text-amber-500 transition-colors">About Us</a>
          <button onClick={() => setIsHelpOpen(true)} className="hover:text-amber-500 transition-colors">Help Center</button>
          <a href="#" className="hover:text-amber-500 transition-colors">Privacy</a>
          <a href="#" className="hover:text-amber-500 transition-colors">Terms</a>
        </div>
      </div>
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 mt-12 border-t border-stone-800 pt-8 text-xs text-center text-stone-600">
        &copy; {new Date().getFullYear()} BookIT. Crafted with elegance.
      </div>
      <HelpCentre isOpen={isHelpOpen} onClose={() => setIsHelpOpen(false)} />
    </footer>
  );
};

export default Footer;
