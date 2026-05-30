export default {
  content: ['./index.html', './src/**/*.{vue,ts,tsx}'],
  theme: {
    extend: {
      colors: {
        primary: {
          50: '#f0fdfa',
          100: '#ccfbf1',
          200: '#99f6e4',
          300: '#5eead4',
          400: '#2dd4bf',
          500: '#14b8a6',
          600: '#0d9488',
          700: '#0f766e',
          800: '#115e59',
          900: '#134e4a'
        }
      },
      boxShadow: {
        card: '0 1px 3px rgba(16,24,40,0.06)',
        'card-hover': '0 10px 40px rgba(16,24,40,0.10)',
        glow: '0 0 24px rgba(20,184,166,0.18)'
      },
      borderRadius: {
        '4xl': '2rem'
      },
      backgroundImage: {
        'brand-gradient': 'linear-gradient(135deg, #14b8a6 0%, #0d9488 100%)'
      }
    }
  },
  plugins: []
}
