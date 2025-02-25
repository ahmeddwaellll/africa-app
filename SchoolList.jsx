import React from 'react';
import {
  Grid,
  Card,
  CardContent,
  Typography,
  CardMedia,
  TextField,
  InputAdornment,
  IconButton,
  Chip,
  Box,
} from '@mui/material';
import SearchIcon from '@mui/icons-material/Search';
import { styled } from '@mui/material/styles';

const StyledCard = styled(Card)(({ theme }) => ({
  transition: 'transform 0.2s',
  '&:hover': {
    transform: 'scale(1.05)',
  },
}));

const SchoolList = () => {
  const schools = [
    {
      id: 1,
      name: 'University of Cape Town',
      location: 'Cape Town, South Africa',
      image: 'https://via.placeholder.com/300',
      description: 'A leading African university.',
    },
    {
      id: 2,
      name: 'Makerere University',
      location: 'Kampala, Uganda',
      image: 'https://via.placeholder.com/300',
      description: 'One of the oldest universities in East Africa.',
    },
    {
      id: 3,
      name: 'University of Nairobi',
      location: 'Nairobi, Kenya',
      image: 'https://via.placeholder.com/300',
      description: 'A premier institution of higher learning in Kenya.',
    },
  ];

  const filters = ['Public', 'Private', 'Engineering', 'Medical', 'Law'];

  return (
    <Box>
      {/* Welcome Section */}
      <Typography variant="h5" gutterBottom>
        Welcome, User!
      </Typography>

      {/* Featured Schools Carousel (Placeholder) */}
      <Box sx={{ mb: 3 }}>
        <Typography variant="subtitle1">Featured Schools</Typography>
        {/* Carousel Implementation Here */}
      </Box>

      {/* Quick Action Cards (Placeholder) */}
      <Grid container spacing={2} sx={{ mb: 3 }}>
        <Grid item xs={6}>
          <Card sx={{ backgroundColor: '#E94D35', color: 'white' }}>
            <CardContent>
              <Typography variant="h6">Explore</Typography>
            </CardContent>
          </Card>
        </Grid>
        <Grid item xs={6}>
          <Card sx={{ backgroundColor: '#1E8449', color: 'white' }}>
            <CardContent>
              <Typography variant="h6">Apply</Typography>
            </CardContent>
          </Card>
        </Grid>
      </Grid>

      {/* Search Bar */}
      <TextField
        fullWidth
        placeholder="Search for schools..."
        InputProps={{
          startAdornment: (
            <InputAdornment position="start">
              <SearchIcon />
            </InputAdornment>
          ),
        }}
        sx={{ mb: 2 }}
      />

      {/* Filter Chips */}
      <Box sx={{ mb: 2 }}>
        {filters.map((filter) => (
          <Chip key={filter} label={filter} sx={{ mr: 1 }} />
        ))}
      </Box>

      {/* School List */}
      <Grid container spacing={3}>
        {schools.map((school) => (
          <Grid item xs={12} sm={6} md={4} key={school.id}>
            <StyledCard>
              <CardMedia component="img" height="140" image={school.image} alt={school.name} />
              <CardContent>
                <Typography variant="h6" component="div">
                  {school.name}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {school.location}
                </Typography>
                <Typography variant="body2" color="text.secondary">
                  {school.description}
                </Typography>
              </CardContent>
            </StyledCard>
          </Grid>
        ))}
      </Grid>
    </Box>
  );
};

export default SchoolList;
