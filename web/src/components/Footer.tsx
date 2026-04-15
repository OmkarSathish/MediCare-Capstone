import { Link } from "react-router-dom";
import { Activity, Mail, Phone, MapPin } from "lucide-react";

export default function Footer() {
  return (
    <footer className="bg-gray-900 text-gray-300">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-14">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-10">
          {/* Brand */}
          <div className="md:col-span-1">
            <div className="flex items-center gap-2 mb-4">
              <div className="bg-blue-600 rounded-xl p-1.5">
                <Activity className="w-5 h-5 text-white" />
              </div>
              <span className="text-white font-bold text-lg">MediCare</span>
            </div>
            <p className="text-sm text-gray-400 leading-relaxed mb-4">
              Comprehensive healthcare diagnostic services — connecting patients
              with trusted diagnostic centers.
            </p>
          </div>

          {/* Quick Links */}
          <div>
            <h4 className="text-white font-semibold mb-4">Quick Links</h4>
            <ul className="space-y-2.5 text-sm">
              {[
                ["Home", "/"],
                ["Diagnostic Centers", "/centers"],
                ["Diagnostic Tests", "/tests"],
                ["Book Appointment", "/book"],
              ].map(([label, href]) => (
                <li key={label}>
                  <Link
                    to={href}
                    className="hover:text-blue-400 transition-colors"
                  >
                    {label}
                  </Link>
                </li>
              ))}
            </ul>
          </div>

          {/* Services */}
          <div>
            <h4 className="text-white font-semibold mb-4">Services</h4>
            <ul className="space-y-2.5 text-sm">
              {[
                "Blood Tests",
                "X-Ray & Imaging",
                "ECG",
                "MRI Scans",
                "Urine Analysis",
                "COVID-19 Testing",
              ].map((s) => (
                <li key={s} className="text-gray-400">
                  {s}
                </li>
              ))}
            </ul>
          </div>

          {/* Contact */}
          <div>
            <h4 className="text-white font-semibold mb-4">Contact</h4>
            <ul className="space-y-3 text-sm">
              <li className="flex items-start gap-2">
                <MapPin className="w-4 h-4 mt-0.5 text-blue-400 shrink-0" />
                <span>123 Health Ave, Medical District, City 10001</span>
              </li>
              <li className="flex items-center gap-2">
                <Phone className="w-4 h-4 text-blue-400 shrink-0" />
                <span>+1 (555) 123-4567</span>
              </li>
              <li className="flex items-center gap-2">
                <Mail className="w-4 h-4 text-blue-400 shrink-0" />
                <span>support@medicare.health</span>
              </li>
            </ul>
          </div>
        </div>

        <hr className="border-gray-800 mt-10 mb-6" />
        <div className="flex flex-col sm:flex-row items-center justify-between text-sm text-gray-500 gap-2">
          <p>
            &copy; {new Date().getFullYear()} MediCare. All rights reserved.
          </p>
          <div className="flex gap-4">
            <a href="#" className="hover:text-blue-400 transition-colors">
              Privacy Policy
            </a>
            <a href="#" className="hover:text-blue-400 transition-colors">
              Terms of Service
            </a>
          </div>
        </div>
      </div>
    </footer>
  );
}
