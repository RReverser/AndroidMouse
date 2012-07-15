using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Net;
using System.Net.Sockets;
using System.Threading;

namespace MouseServer
{
    class Program
    {
        private const int MAX_X_ANGLE = 40;
        private const int MAX_Y_ANGLE = -20;

        private static int NormalizeAngle(int angle, int maxAngle)
        {
            var relAngle = (double)angle / (2 * maxAngle) + 0.5;
            relAngle = Math.Max(0, Math.Min(1, relAngle));
            return (int)Math.Round(relAngle * UInt16.MaxValue);
        }

        public static void StartListening()
        {
            var listener = new Socket(AddressFamily.InterNetwork, SocketType.Stream, ProtocolType.Tcp);
            var rawData = new byte[14];
            var points = new Mouse.Point[12];
            var angleData = new int[3];
            bool leftPressed = false, rightPressed = false;

            try
            {
                listener.Bind(new IPEndPoint(IPAddress.Any, 5382));
                listener.Listen(10);

                while (!Console.KeyAvailable)
                {
                    Console.WriteLine("Waiting for connection");
                    var handler = listener.Accept();
                    Console.WriteLine("{0} connected", handler.RemoteEndPoint);
                    try
                    {
                        while (handler.Receive(rawData) >= rawData.Length && !Console.KeyAvailable)
                        {
                            Buffer.BlockCopy(rawData.Take(12).Reverse().ToArray(), 0, angleData, 0, 12);

                            bool newLeftPressed = rawData[12] != 0, newRightPressed = rawData[13] != 0;

                            for (var i = 0; i < points.Length - 1; i++) 
                            {
                                points[i] = points[i + 1];
                            }

                            points[points.Length - 1] =
                                new Mouse.Point
                                {
                                    X = NormalizeAngle(angleData[2], MAX_X_ANGLE),
                                    Y = NormalizeAngle(angleData[1], MAX_Y_ANGLE)
                                };

                            if (Math.Pow(points[points.Length - 1].X - points[0].X, 2) + Math.Pow(points[points.Length - 1].Y - points[0].Y, 2) > 9000)
                            {
                                Mouse.Move((uint)points.Average(point => point.X), (uint)points.Average(point => point.Y));
                            }

                            if (angleData[0] != 0)
                            {
                                Mouse.Wheel((int)Math.Round(0.6 * angleData[0]));
                            }

                            if (newLeftPressed != leftPressed)
                            {
                                if (newLeftPressed) Mouse.LeftDown(); else Mouse.LeftUp();
                                leftPressed = newLeftPressed;
                            }

                            if (newRightPressed != rightPressed)
                            {
                                if (newRightPressed) Mouse.RightDown(0, 0); else Mouse.RightUp(0, 0);
                                rightPressed = newRightPressed;
                            }
                        }

                        handler.Disconnect(true);
                    }
                    catch (Exception innerE)
                    {
                        Console.WriteLine(innerE.ToString());
                    }
                }

            }
            catch (Exception e)
            {
                Console.WriteLine(e.ToString());
            }

            Console.ReadKey();
        }

        static void Main(string[] args)
        {
            Console.Title = "Mouse Server";
            StartListening();
        }
    }
}
