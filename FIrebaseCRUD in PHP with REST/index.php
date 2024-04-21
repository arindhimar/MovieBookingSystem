<?php

class FirebaseRestManager {
    private $firebaseUrl;

    public function __construct($firebaseUrl) {
        $this->firebaseUrl = $firebaseUrl;
    }

    public function addItem($item) {
        $data = json_encode($item);
        $ch = curl_init($this->firebaseUrl . '/items.json');
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "POST");
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        curl_close($ch);
        return $response;
    }

    public function getAllItems() {
        $ch = curl_init($this->firebaseUrl . '/items.json');
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        curl_close($ch);
        return json_decode($response, true);
    }

    public function updateItem($itemId, $newItem) {
        $data = json_encode($newItem);
        $ch = curl_init($this->firebaseUrl . '/items/' . $itemId . '.json');
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "PUT");
        curl_setopt($ch, CURLOPT_POSTFIELDS, $data);
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        curl_close($ch);
        return $response;
    }

    public function deleteItem($itemId) {
        $ch = curl_init($this->firebaseUrl . '/items/' . $itemId . '.json');
        curl_setopt($ch, CURLOPT_CUSTOMREQUEST, "DELETE");
        curl_setopt($ch, CURLOPT_RETURNTRANSFER, true);
        $response = curl_exec($ch);
        curl_close($ch);
        return $response;
    }
}

// Example usage
$firebaseUrl = "https://my-application-66768-default-rtdb.firebaseio.com";
$firebaseRestManager = new FirebaseRestManager($firebaseUrl);

// Add item
// $newItem = array("name" => "Item 1", "description" => "Description of item 1");
// $response = $firebaseRestManager->addItem($newItem);
// echo "Add Item Response: " . $response . "\n";

// // Get all items
// $allItems = $firebaseRestManager->getAllItems();
// echo "All Items: \n";
// print_r($allItems);

// // Update item
// $updatedItem = array("name" => "Updated Item", "description" => "Updated description");
// $response = $firebaseRestManager->updateItem("-Nvzea_cT4zp29HSQBE2", $updatedItem); // Replace with actual item ID
// echo "Update Item Response: " . $response . "\n";

// // Delete item
// $response = $firebaseRestManager->deleteItem("-Nvzea_cT4zp29HSQBE2"); // Replace with actual item ID
// echo "Delete Item Response: " . $response . "\n";
?>
