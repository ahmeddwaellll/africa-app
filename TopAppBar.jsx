import React from 'react';
import { AppBar, Toolbar, IconButton, Typography, Box } from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import SettingsIcon from '@mui/icons-material/Settings';
import AccountCircle from '@mui/icons-material/AccountCircle';
import { useNavigate } from 'react-router-dom';
import { useTheme } from '@mui/material/styles';

function TopAppBar() {
  const navigate = useNavigate();
  const theme = useTheme();

  return (
    <AppBar
      position="static"
      sx={{
        backgroundColor: theme.palette.primary.main, // Deep Orange/Terra Cotta
        boxShadow: theme.shadows[3],
        padding: '0.5rem 0',
      }}
    >
      <Toolbar sx={{ padding: '0 1rem' }}>
        <Typography
          variant="h6"
          component="div"
          sx={{
            flexGrow: 1,
            fontWeight: 500,
            letterSpacing: '0.1em',
            color: theme.palette.lightSand.main,
          }}
        >
          African Schools
        </Typography>
        <IconButton color="inherit" onClick={() => navigate('/search')}>
          <SearchIcon sx={{ color: theme.palette.lightSand.main }} />
        </IconButton>
        <IconButton color="inherit" onClick={() => navigate('/profile')}>
          <AccountCircle sx={{ color: theme.palette.lightSand.main }} />
        </IconButton>
        <IconButton color="inherit" onClick={() => navigate('/settings')}>
          <SettingsIcon sx={{ color: theme.palette.lightSand.main }} />
        </IconButton>
      </Toolbar>
    </AppBar>
  );
}

export default TopAppBar;
