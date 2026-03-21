#!/usr/bin/env python3
"""
Base client for interacting with the Remixed Dungeon WebServer debug API.

Usage:
    from game_client import GameClient

    client = GameClient()
    client.start_game("DOCTOR")
    state = client.get_game_state()
"""

import json
import requests
import time
from typing import Optional, Dict, Any, List


class GameClient:
    """Client for Remixed Dungeon WebServer debug endpoints."""

    def __init__(self, host: str = "localhost", port: int = 8080):
        self.base_url = f"http://{host}:{port}"
        self.session = requests.Session()

    def _get(self, endpoint: str) -> Dict[str, Any]:
        """Make a GET request to an endpoint."""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.get(url, timeout=10)
            return response.json()
        except Exception as e:
            return {"error": str(e)}

    def _post(self, endpoint: str) -> Dict[str, Any]:
        """Make a POST request to an endpoint."""
        url = f"{self.base_url}{endpoint}"
        try:
            response = self.session.post(url, timeout=10)
            return response.json()
        except Exception as e:
            return {"error": str(e)}

    def check_server(self) -> bool:
        """Check if the webserver is running."""
        try:
            url = f"{self.base_url}/"
            response = self.session.get(url, timeout=5)
            return response.status_code == 200
        except:
            return False

    # Game Control
    def start_game(self, hero_class: str = "WARRIOR", difficulty: int = 0) -> Dict[str, Any]:
        """Start a new game with specified hero class."""
        return self._get(f"/debug/start_game?class={hero_class}&difficulty={difficulty}")

    def get_game_state(self) -> Dict[str, Any]:
        """Get current game state."""
        return self._get("/debug/get_game_state")

    def get_hero_info(self) -> Dict[str, Any]:
        """Get detailed hero information."""
        return self._get("/debug/get_hero_info")

    def get_level_info(self) -> Dict[str, Any]:
        """Get current level information."""
        return self._get("/debug/get_level_info")

    # Spell Testing
    def get_available_spells(self) -> Dict[str, Any]:
        """Get list of available spells."""
        return self._get("/debug/get_available_spells")

    def cast_spell(self, spell_type: str) -> Dict[str, Any]:
        """Cast a spell by name."""
        return self._get(f"/debug/cast_spell?type={spell_type}")

    def cast_spell_on_target(self, spell_type: str, x: int, y: int) -> Dict[str, Any]:
        """Cast a spell on a specific target position."""
        return self._get(f"/debug/cast_spell_on_target?type={spell_type}&x={x}&y={y}")

    # Mob Management
    def get_mobs(self) -> Dict[str, Any]:
        """Get mobs on current level."""
        return self._get("/debug/get_mobs")

    def create_mob(self, mob_type: str) -> Dict[str, Any]:
        """Create a mob on the level."""
        return self._get(f"/debug/create_mob?type={mob_type}")

    def kill_mob(self, x: int, y: int) -> Dict[str, Any]:
        """Kill a mob at specified coordinates."""
        return self._get(f"/debug/kill_mob?x={x}&y={y}")

    # Item Management
    def get_items(self) -> Dict[str, Any]:
        """Get items on current level."""
        return self._get("/debug/get_items")

    def get_inventory(self) -> Dict[str, Any]:
        """Get hero inventory."""
        return self._get("/debug/get_inventory")

    def create_item(self, item_type: str, x: int = -1, y: int = -1) -> Dict[str, Any]:
        """Create an item, optionally at specific position."""
        if x >= 0 and y >= 0:
            return self._get(f"/debug/create_item?type={item_type}&x={x}&y={y}")
        return self._get(f"/debug/create_item?type={item_type}")

    def give_item(self, item_type: str) -> Dict[str, Any]:
        """Give an item to the hero."""
        return self._get(f"/debug/give_item?type={item_type}")

    # Debugging
    def get_recent_logs(self) -> Dict[str, Any]:
        """Get recent log messages."""
        return self._get("/debug/get_recent_logs")

    def set_hero_stat(self, stat: str, value: int) -> Dict[str, Any]:
        """Set a hero stat value."""
        return self._get(f"/debug/set_hero_stat?stat={stat}&value={value}")

    # Level Control
    def change_level(self, depth: int) -> Dict[str, Any]:
        """Change to a specific dungeon depth."""
        return self._get(f"/debug/change_level?depth={depth}")

    # Movement and Combat
    def get_mob_positions(self) -> Dict[str, Any]:
        """Get mob positions in simple format."""
        return self._get("/debug/get_mob_positions")

    def get_hero_position(self) -> Dict[str, Any]:
        """Get hero position."""
        return self._get("/debug/get_hero_position")

    def move_hero(self, x: int, y: int) -> Dict[str, Any]:
        """Move hero to coordinates."""
        return self._get(f"/debug/move_hero?x={x}&y={y}")

    def hero_attack(self, x: int, y: int) -> Dict[str, Any]:
        """Hero attacks mob at position."""
        return self._get(f"/debug/hero_attack?x={x}&y={y}")

    def wait_ticks(self, ticks: int = 10) -> Dict[str, Any]:
        """Wait N game ticks."""
        return self._get(f"/debug/wait_ticks?ticks={ticks}")

    # Hero Classes
    HERO_CLASSES = [
        "WARRIOR",
        "MAGE",
        "ROGUE",
        "HUNTRESS",
        "ELF",
        "NECROMANCER",
        "GNOLL",
        "PRIEST",
        "DOCTOR",
    ]
