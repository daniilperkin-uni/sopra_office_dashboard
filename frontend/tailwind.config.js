/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{vue,js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      screens: {
        '4k': '3840px',
      },
      colors: {
        primary: '#009ee2',
        'primary-dark': '#008bc7',
        'neutral-bg': '#F5F7FA',
        'text-dark': '#102A43',

        // Additional colors for the application
        'red': '#ef4444',
        'red-500': '#ef4444',
        'red-600': '#dc2626',
        'green': '#10b981',
        'green-500': '#10b981',
        'green-600': '#059669',
        'yellow': '#eab308',
        'yellow-500': '#eab308',
        'gray': '#6b7280',
        'gray-200': '#e5e7eb',
        'gray-300': '#d1d5db',
        'gray-500': '#6b7280',
        'gray-600': '#4b5563',
        'gray-700': '#374151',
        'white': '#ffffff',
        'black': '#000000',
      },
      fontFamily: {
        sans: ['Mulish', 'system-ui', 'sans-serif'],
      },
    },
  },
  plugins: [],
}
