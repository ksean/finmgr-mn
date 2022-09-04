import Grid from "@mui/material/Grid";
import Paper from "@mui/material/Paper";
import * as React from "react";

export default function Import() {
    return (
        <Grid container spacing={3}>
            {/* Import */}
            <Grid item xs={12}>
                <Paper sx={{p: 2, display: 'flex', flexDirection: 'column'}}>
                    Import
                </Paper>
            </Grid>
        </Grid>
    );
}