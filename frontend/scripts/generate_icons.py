import struct
import zlib
import os

def write_png(width, height, filename, r, g, b):
    # PNG Header
    header = b'\x89PNG\r\n\x1a\n'

    # IHDR Chunk
    # Width (4), Height (4), BitDepth (1), ColorType (1), Compression (1), Filter (1), Interlace (1)
    ihdr_data = struct.pack('!IIBBBBB', width, height, 8, 2, 0, 0, 0)
    ihdr_crc = zlib.crc32(b'IHDR' + ihdr_data)
    ihdr = struct.pack('!I', len(ihdr_data)) + b'IHDR' + ihdr_data + struct.pack('!I', ihdr_crc)

    # IDAT Chunk (Image Data)
    # Scanline: [FilterByte (0)] + [RGB] * width
    raw_data = b''
    for _ in range(height):
        raw_data += b'\x00' # No filter
        raw_data += struct.pack('BBB', r, g, b) * width

    compressed_data = zlib.compress(raw_data)
    idat_crc = zlib.crc32(b'IDAT' + compressed_data)
    idat = struct.pack('!I', len(compressed_data)) + b'IDAT' + compressed_data + struct.pack('!I', idat_crc)

    # IEND Chunk
    iend_crc = zlib.crc32(b'IEND')
    iend = struct.pack('!I', 0) + b'IEND' + struct.pack('!I', iend_crc)

    with open(filename, 'wb') as f:
        f.write(header)
        f.write(ihdr)
        f.write(idat)
        f.write(iend)

    print(f"Generated {filename}")

if __name__ == "__main__":
    # Ensure directory exists
    os.makedirs('frontend/public', exist_ok=True)

    # Generate 192x192 (Primary Color #0d6efd -> R=13, G=110, B=253)
    write_png(192, 192, 'frontend/public/pwa-192x192.png', 13, 110, 253)

    # Generate 512x512
    write_png(512, 512, 'frontend/public/pwa-512x512.png', 13, 110, 253)
