import React from 'react';
import { useParams } from 'react-router-dom';
import { Container, Typography } from '@mui/material';

function SchoolDetail() {
  const { id } = useParams();

  return (
    <Container maxWidth="md" sx={{ mt: 4 }}>
      <Typography variant="h4" gutterBottom>
        School Detail for ID: {id}
      </Typography>
      <Typography variant="body1">
        Details of the school will be displayed here.
      </Typography>
    </Container>
  );
}

export default SchoolDetail;
