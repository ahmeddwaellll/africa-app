import React from 'react';
import { BottomNavigation as MuiBottomNavigation, BottomNavigationAction, Paper } from '@mui/material';
import { Home, School, Person, Settings } from '@mui/icons-material';
import { useNavigate, useLocation } from 'react-router-dom';
import { useTheme } from '@mui/material/styles';

function BottomNavigation() {
  const navigate = useNavigate();
  const location = useLocation();
  const theme = useTheme();

  const getValue = () => {
    const path = location.pathname;
    switch (path) {
      case '/':
        return 0;
      case '/profile':
        return 1;
      case '/settings':
        return 2;
      default:
        return 0;
    }
  };

  const [value, setValue] = React.useState(getValue());

  React.useEffect(() => {
    setValue(getValue());
  }, [location.pathname]);

  const handleChange = (event, newValue) => {
    setValue(newValue);
    switch (newValue) {
      case 0:
        navigate('/');
        break;
      case 1:
        navigate('/profile');
        break;
      case 2:
        navigate('/settings');
        break;
      default:
        navigate('/');
    }
  };

  return (
    <Paper
      sx={{
        position: 'fixed',
        bottom: 0,
        left: 0,
        right: 0,
        zIndex: 1000,
        backgroundColor: theme.palette.white.main, // White background
      }}
      elevation={3}
    >
      <MuiBottomNavigation
        value={value}
        onChange={handleChange}
        showLabels
        sx={{
          backgroundColor: 'transparent',
          color: theme.palette.secondary.main,
        }}
      >
        <BottomNavigationAction
          label="Home"
          icon={<Home />}
          sx={{ color: theme.palette.secondary.main, '&.Mui-selected': { color: theme.palette.forestGreen.main } }}
        />
        <BottomNavigationAction
          label="Profile"
          icon={<Person />}
          sx={{ color: theme.palette.secondary.main, '&.Mui-selected': { color: theme.palette.forestGreen.main } }}
        />
        <BottomNavigationAction
          label="Settings"
          icon={<Settings />}
          sx={{ color: theme.palette.secondary.main, '&.Mui-selected': { color: theme.palette.forestGreen.main } }}
        />
      </MuiBottomNavigation>
    </Paper>
  );
}

export default BottomNavigation;
