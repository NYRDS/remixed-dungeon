#!/usr/bin/env python3
"""
Benchmark script to compare original and optimized versions
"""
import time
import os
import sys

# Add the tools directory to the path so we can import both scripts
sys.path.insert(0, os.path.join(os.path.dirname(__file__), 'tools/py-tools'))

from wiki_potential_links import find_all_potential_links as find_all_potential_links_original
from wiki_potential_links_optimized import find_all_potential_links as find_all_potential_links_optimized

def benchmark_function(func, wiki_dir, name):
    """Benchmark a function and return its execution time."""
    print(f"Starting benchmark for {name}...")
    start_time = time.time()
    result = func(wiki_dir)
    end_time = time.time()
    execution_time = end_time - start_time
    print(f"{name} completed in {execution_time:.2f} seconds")
    return execution_time, result

def main():
    wiki_data_dir = "wiki-data/pages"
    
    if not os.path.exists(wiki_data_dir):
        print(f"Wiki directory {wiki_data_dir} not found.")
        wiki_data_dir = "."
    
    print(f"Benchmarking with wiki directory: {wiki_data_dir}")
    
    # Benchmark original version (first run might be slower due to disk caching)
    orig_time, orig_result = benchmark_function(
        find_all_potential_links_original, 
        wiki_data_dir, 
        "Original"
    )
    
    print()
    
    # Benchmark optimized version
    opt_time, opt_result = benchmark_function(
        find_all_potential_links_optimized, 
        wiki_data_dir, 
        "Optimized"
    )
    
    print("\n" + "="*50)
    print("BENCHMARK RESULTS:")
    print(f"Original version:  {orig_time:.2f} seconds")
    print(f"Optimized version: {opt_time:.2f} seconds")
    print(f"Speed improvement: {orig_time/opt_time:.2f}x faster")
    print(f"Time saved: {orig_time - opt_time:.2f} seconds")
    print("="*50)

if __name__ == "__main__":
    main()