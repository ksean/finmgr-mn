/*
    finmgr - a financial management framework
    Copyright (C) 2022  Kennedy Software Solutions Inc.

    This program is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    This program is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with this program.  If not, see <https://www.gnu.org/licenses/>.

    sean <at> kennedy <dot> software
 */
import * as React from 'react';
import {useEffect, useState} from 'react';
import { useTheme } from '@mui/material/styles';
import {Label, Line, LineChart, ResponsiveContainer, XAxis, YAxis} from 'recharts';
import Title from '../Title';

export default function PortfolioHistory() {
    const theme = useTheme()

    const [data, setData] = useState([])
    const [error, setError] = useState(null)
    const [isLoading, setIsLoading] = useState(true)

    useEffect(() => {
        fetch('/report/latest', {
            mode: 'no-cors'
        })
            .then(response => {
                console.log(response)
                if (response.status === 200) {
                    response.json().then (data => {
                        setData(data)
                        setIsLoading(false)
                    })
                } else if (response.status === 500) {
                    setIsLoading(false)
                    setError({'message': 'API server isn\'t started'})
                } else {
                    setIsLoading(false)
                    setError(response)
                }
            })
            .catch(error => {
                console.log(error)
                setIsLoading(false)
                setError(error)
            })
        }, []);

    return (
        <React.Fragment>
            <Title>Portfolio History</Title>
            <ResponsiveContainer>
                <LineChart
                    data={data}
                    width={500}
                    height={500}
                    margin={{
                        top: 16,
                        right: 16,
                        bottom: 0,
                        left: 24,
                    }}>
                    <XAxis
                        dataKey="date"
                        stroke={theme.palette.text.secondary}
                        style={theme.typography.body2}/>
                    <YAxis
                        stroke={theme.palette.text.secondary}
                        style={theme.typography.body2}>
                        <Label
                            angle={270}
                            position="left"
                            style={{
                                textAnchor: 'middle',
                                fill: theme.palette.text.primary,
                                ...theme.typography.body1,
                            }}>
                            Holdings ($)
                        </Label>
                    </YAxis>
                    <Line
                        isAnimationActive={false}
                        type="monotone"
                        dataKey="amount"
                        stroke={theme.palette.primary.main}
                        dot={false}/>
                </LineChart>
            </ResponsiveContainer>
        </React.Fragment>
    );
}