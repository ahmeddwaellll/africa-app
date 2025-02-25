import React from 'react';
import { BrowserRouter as Router, Route, Routes } from 'react-router-dom';
import { Container } from '@mui/material';
import { ThemeProvider, createTheme } from '@mui/material/styles';
import TopAppBar from './components/TopAppBar';
import BottomNavigation from './components/BottomNavigation';
import SchoolList from './components/SchoolList';
import SchoolDetail from './components/SchoolDetail';
import Login from './components/Login';
import Register from './components/Register';
import Profile from './components/Profile';
import Settings from './components/Settings';

const theme = createTheme({
  palette: {
    primary: {
      main: '#E94D35', // Deep Orange/Terra Cotta
    },
    secondary: {
      main: '#607D8B', // Adjusted secondary color
    },
    lightSand: {
      main: '#F5DEB3',
    },
    gold: {
      main: '#F4D03F',
    },
    forestGreen: {
      main: '#1E8449',
    },
    deepBlue: {
      main: '#2E86C1',
    },
    warmBrown: {
      main: '#8B4513',
    },
    neutralGray: {
      main: '#808B96',
    },
    brightOrange: {
      main: '#FF8C00',
    },
    white: {
      main: '#FFFFFF',
    },
    text: {
      primary: '#2E3B55',
      secondary: '#607D8B',
    },
  },
  typography: {
    fontFamily: 'Roboto, sans-serif',
  },
  components: {
    MuiButton: {
      styleOverrides: {
        root: {
          borderRadius: '24px',
          padding: '8px 24px',
          textTransform: 'none',
        },
      },
    },
    MuiCard: {
      styleOverrides: {
        root: {
          borderRadius: '12px',
          boxShadow: '0px 4px 12px rgba(0, 0, 0, 0.1)',
        },
      },
    },
  },
});

function App() {
  return (
    <ThemeProvider theme={theme}>
      <Router>
        <TopAppBar />
        <Container maxWidth="md" sx={{ mt: 4, mb: 10 }}>
          <Routes>
            <Route path="/" element={<SchoolList />} />
            <Route path="/school/:id" element={<SchoolDetail />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            <Route path="/profile" element={<Profile />} />
            <Route path="/settings" element={<Settings />} />
          </Routes>
        </Container>
        <BottomNavigation />
      </Router>
    </ThemeProvider>
  );
}

export default App;
